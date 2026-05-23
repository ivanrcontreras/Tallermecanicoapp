package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class DatabaseRepository(private val dao: WorkshopDao) {

    // Clients
    val allClients: Flow<List<Client>> = dao.getAllClients()

    suspend fun getClientByPlate(plate: String): Client? {
        return dao.getClientByPlate(plate.trim())
    }

    suspend fun insertClient(client: Client): Long {
        return dao.insertClient(client.copy(plate = client.plate.trim().uppercase()))
    }

    suspend fun deleteClient(client: Client) {
        dao.deleteClient(client)
    }

    // Maintenances
    val allMaintenances: Flow<List<Maintenance>> = dao.getAllMaintenances()

    fun getMaintenancesByPlate(plate: String): Flow<List<Maintenance>> {
        return dao.getMaintenancesByPlate(plate.trim().uppercase())
    }

    suspend fun insertMaintenance(maintenance: Maintenance): Long {
        return dao.insertMaintenance(maintenance.copy(clientPlate = maintenance.clientPlate.trim().uppercase()))
    }

    // Components
    val allComponents: Flow<List<Component>> = dao.getAllComponents()

    suspend fun insertComponent(component: Component): Long {
        return dao.insertComponent(component)
    }

    suspend fun deleteComponentById(id: Int) {
        dao.deleteComponentById(id)
    }

    // Vehicle Checks (Reports)
    val allVehicleChecks: Flow<List<VehicleCheck>> = dao.getAllVehicleChecks()

    fun getVehicleChecksByPlate(plate: String): Flow<List<VehicleCheck>> {
        return dao.getVehicleChecksByPlate(plate.trim().uppercase())
    }

    suspend fun getVehicleCheckById(id: Int): VehicleCheck? {
        return dao.getVehicleCheckById(id)
    }

    suspend fun insertVehicleCheck(check: VehicleCheck): Long {
        return dao.insertVehicleCheck(check.copy(clientPlate = check.clientPlate.trim().uppercase()))
    }

    // Component Checks inside reports
    suspend fun getComponentChecksForReport(checkId: Int): List<ComponentCheck> {
        return dao.getComponentChecksForReport(checkId)
    }

    suspend fun insertComponentChecks(checks: List<ComponentCheck>) {
        dao.insertComponentChecks(checks)
    }

    // Seed default components if the catalog is empty
    suspend fun seedDefaultComponentsIfNeeded() {
        val existing = dao.getAllComponents().first()
        if (existing.isEmpty()) {
            val defaults = listOf(
                Component(name = "Frenos delanteros y traseros", isLevelCheck = false),
                Component(name = "Filtro de Aceite", isLevelCheck = false),
                Component(name = "Filtro de Aire", isLevelCheck = false),
                Component(name = "Luces e Indicadores", isLevelCheck = false),
                Component(name = "Neumáticos y Presión", isLevelCheck = false),
                Component(name = "Amortiguadores y Suspensión", isLevelCheck = false),
                Component(name = "Pastillas de Freno", isLevelCheck = false),
                Component(name = "Batería y Bornes", isLevelCheck = false),
                
                Component(name = "Nivel de Aceite de Motor", isLevelCheck = true),
                Component(name = "Nivel de Líquido de Frenos", isLevelCheck = true),
                Component(name = "Nivel de Líquido Refrigerante", isLevelCheck = true),
                Component(name = "Nivel de Agua de Limpiaparabrisas", isLevelCheck = true),
                Component(name = "Nivel de Líquido de Dirección", isLevelCheck = true)
            )
            for (comp in defaults) {
                dao.insertComponent(comp)
            }
        }
    }
}
