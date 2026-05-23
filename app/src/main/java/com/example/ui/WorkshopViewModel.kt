package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.pdf.PdfGenerator
import com.example.pdf.WorkshopNotificationManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class WorkshopViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = DatabaseRepository(database.workshopDao)

    // Data Flows
    val clients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenances: StateFlow<List<Maintenance>> = repository.allMaintenances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val components: StateFlow<List<Component>> = repository.allComponents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehicleChecks: StateFlow<List<VehicleCheck>> = repository.allVehicleChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Status messaging
    var actionMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.seedDefaultComponentsIfNeeded()
            checkMaintenanceAlertsOnStartup()
        }
    }

    fun showMessage(message: String) {
        actionMessage = message
    }

    fun clearMessage() {
        actionMessage = null
    }

    // Client CRUD
    fun addClient(name: String, phone: String, email: String, vehicle: String, plate: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank() || plate.isBlank() || vehicle.isBlank()) {
                showMessage("Por favor, completa los campos requeridos (Nombre, Teléfono, Auto y Patente).")
                return@launch
            }
            val cleanPlate = plate.trim().uppercase()
            val existing = repository.getClientByPlate(cleanPlate)
            if (existing != null) {
                showMessage("La patente '$cleanPlate' ya está registrada para el cliente ${existing.name}.")
                return@launch
            }

            repository.insertClient(
                Client(
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    vehicle = vehicle.trim(),
                    plate = cleanPlate
                )
            )
            showMessage("Cliente registrado con éxito.")
            onSuccess()
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
            showMessage("Cliente eliminado de la base de datos.")
        }
    }

    // Maintenance CRUD
    fun addMaintenance(plate: String, currentKm: Int, nextServiceKm: Int, observation: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val cleanPlate = plate.trim().uppercase()
            if (cleanPlate.isBlank()) {
                showMessage("La patente es requerida.")
                return@launch
            }
            val client = repository.getClientByPlate(cleanPlate)
            if (client == null) {
                showMessage("Vehículo no encontrado. Registre el cliente primero.")
                return@launch
            }
            if (nextServiceKm <= currentKm) {
                showMessage("El KM del próximo servicio debe ser mayor al KM de ingreso.")
                return@launch
            }

            val todayStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            repository.insertMaintenance(
                Maintenance(
                    clientPlate = cleanPlate,
                    currentKm = currentKm,
                    nextServiceKm = nextServiceKm,
                    datePerformed = todayStr,
                    observation = observation.trim()
                )
            )
            
            // Notification Engine trigger
            val remainingKm = nextServiceKm - currentKm
            if (remainingKm <= 1500) {
                WorkshopNotificationManager.triggerMaintenanceNotification(
                    getApplication(),
                    client.name,
                    cleanPlate,
                    remainingKm,
                    nextServiceKm
                )
            }

            showMessage("Mantenimiento programado guardado correctamente.")
            onSuccess()
        }
    }

    // Component CRUD
    fun addComponent(name: String, isLevelCheck: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showMessage("El nombre del componente es obligatorio.")
                return@launch
            }
            repository.insertComponent(
                Component(
                    name = name.trim(),
                    isLevelCheck = isLevelCheck
                )
            )
            showMessage("Componente añadido al catálogo de revisiones.")
            onSuccess()
        }
    }

    fun deleteComponent(component: Component) {
        viewModelScope.launch {
            repository.deleteComponentById(component.id)
            showMessage("Componente eliminado del catálogo.")
        }
    }

    // ACTIVE INSPECTION (Chequeo Vehicular) STATE
    var queryPlate by mutableStateOf("")
    var matchedClientForCheck by mutableStateOf<Client?>(null)
        private set

    // Temporary mappings to hold mechanics choice during diagnostic session
    // componentId -> String (e.g. "Bueno", "Regular", "Malo" or "A Nivel", "Bajo Nivel")
    val componentCheckStatuses = mutableStateMapOf<Int, String>()
    // componentId -> Observation
    val componentCheckObservations = mutableStateMapOf<Int, String>()
    var generalMechanicObservation by mutableStateOf("")

    fun searchVehicleForCheck() {
        viewModelScope.launch {
            val cleanPlate = queryPlate.trim().uppercase()
            if (cleanPlate.isBlank()) {
                showMessage("Ingrese una patente para buscar.")
                matchedClientForCheck = null
                return@launch
            }
            val client = repository.getClientByPlate(cleanPlate)
            if (client != null) {
                matchedClientForCheck = client
                initializeChecklist()
                showMessage("Vehículo encontrado: ${client.vehicle} de ${client.name}")
            } else {
                matchedClientForCheck = null
                showMessage("No se encontró ningún cliente registrado con la patente '$cleanPlate'.")
            }
        }
    }

    private fun initializeChecklist() {
        componentCheckStatuses.clear()
        componentCheckObservations.clear()
        generalMechanicObservation = ""
        
        viewModelScope.launch {
            val list = repository.allComponents.first()
            for (comp in list) {
                componentCheckStatuses[comp.id] = if (comp.isLevelCheck) "A Nivel" else "Bueno"
                componentCheckObservations[comp.id] = ""
            }
        }
    }

    fun setComponentStatus(componentId: Int, status: String) {
        componentCheckStatuses[componentId] = status
    }

    fun setComponentObservation(componentId: Int, observation: String) {
        componentCheckObservations[componentId] = observation
    }

    // SAVE COMPLETE CHECKUP & LAUNCH SHARE PDF
    fun saveCheckupAndShare(context: Context, onSuccess: (File) -> Unit = {}) {
        val client = matchedClientForCheck
        if (client == null) {
            showMessage("Error: No hay un vehículo de chequeo activo.")
            return
        }

        viewModelScope.launch {
            val todayStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            
            // 1. Save general VehicleCheck header
            val checkId = repository.insertVehicleCheck(
                VehicleCheck(
                    clientPlate = client.plate,
                    datePerformed = todayStr,
                    mechanicObservation = generalMechanicObservation
                )
            ).toInt()

            // 2. Accumulate individual ComponentCheck results
            val list = repository.allComponents.first()
            val checksToSave = mutableListOf<ComponentCheck>()
            for (comp in list) {
                val status = componentCheckStatuses[comp.id] ?: (if (comp.isLevelCheck) "A Nivel" else "Bueno")
                val obs = componentCheckObservations[comp.id] ?: ""
                checksToSave.add(
                    ComponentCheck(
                        vehicleCheckId = checkId,
                        componentName = comp.name,
                        isLevelCheck = comp.isLevelCheck,
                        status = status,
                        observation = obs
                    )
                )
            }
            repository.insertComponentChecks(checksToSave)

            // Get latest maintenance info if exists, to embed in PDF
            val maints = repository.getMaintenancesByPlate(client.plate).first()
            val latestMaint = maints.firstOrNull()

            // 3. Generate PDF
            val pdfFile = PdfGenerator.generateVehicleCheckPdf(
                context = context,
                client = client,
                check = VehicleCheck(id = checkId, clientPlate = client.plate, datePerformed = todayStr, mechanicObservation = generalMechanicObservation),
                results = checksToSave,
                maintenance = latestMaint
            )

            if (pdfFile != null && pdfFile.exists()) {
                showMessage("¡Diagnóstico guardado y PDF generado con éxito!")
                onSuccess(pdfFile)
                // Direct call to triggers
                PdfGenerator.sharePdf(context, pdfFile, client.name, client.plate)
            } else {
                showMessage("Error al crear el archivo PDF.")
            }
        }
    }

    // Startup warnings or checks
    private fun checkMaintenanceAlertsOnStartup() {
        viewModelScope.launch {
            val clList = repository.allClients.first()
            for (cl in clList) {
                val mList = repository.getMaintenancesByPlate(cl.plate).first()
                val latest = mList.firstOrNull()
                if (latest != null) {
                    val remaining = latest.nextServiceKm - latest.currentKm
                    if (remaining <= 1000) {
                        // Post a warn notification
                        WorkshopNotificationManager.triggerMaintenanceNotification(
                            getApplication(),
                            cl.name,
                            cl.plate,
                            remaining,
                            latest.nextServiceKm
                        )
                    }
                }
            }
        }
    }
}
