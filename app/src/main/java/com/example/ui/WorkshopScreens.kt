package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Client
import com.example.data.Component
import com.example.data.Maintenance
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(viewModel: WorkshopViewModel, onSelectPlateForInspection: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }

    var filterQuery by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    val clientsList by viewModel.clients.collectAsState()

    val filteredClients = clientsList.filter {
        it.name.contains(filterQuery, ignoreCase = true) ||
        it.plate.contains(filterQuery, ignoreCase = true) ||
        it.vehicle.contains(filterQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Clientes y Vehículos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showForm) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("toggle_client_form_button")
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (showForm) "Cerrar" else "Nuevo",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showForm) "Cancelar" else "Nuevo Cliente")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Form to add client
        AnimatedVisibility(
            visible = showForm,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Registrar Cliente & Carro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Completo (Obligatorio)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Teléfono / WhatsApp") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = plate,
                            onValueChange = { plate = it },
                            label = { Text("Patente / Chapa") },
                            placeholder = { Text("AE123XX") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_plate_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = vehicle,
                        onValueChange = { vehicle = it },
                        label = { Text("Modelo del Vehículo (ej. Ford Ranger)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_vehicle_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.addClient(name, phone, email, vehicle, plate) {
                                // Reset inputs
                                name = ""
                                phone = ""
                                email = ""
                                vehicle = ""
                                plate = ""
                                showForm = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_client_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Guardar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Vehículo y Cliente", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            label = { Text("Buscar por nombre, modelo o patente...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("client_search_input"),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Clients List
        if (filteredClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "No hay vehículos",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (clientsList.isEmpty()) "No hay clientes registrados." else "No se encontraron coincidencias.",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredClients) { client ->
                    ClientCard(
                        client = client,
                        onDelete = { viewModel.deleteClient(client) },
                        onInspect = { onSelectPlateForInspection(client.plate) }
                    )
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: Client, onDelete: () -> Unit, onInspect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("client_card_${client.plate}"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = client.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = client.vehicle,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Patent Badge - Bento styled
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = client.plate,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tel: ${client.phone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (client.email.isNotEmpty()) {
                        Text(
                            text = "Email: ${client.email}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.offset(y = (-4).dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_client_${client.plate}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Button(
                        onClick = onInspect,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("inspect_client_${client.plate}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chequear", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ------------------- MAINTENANCE TAB -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(viewModel: WorkshopViewModel) {
    var plate by remember { mutableStateOf("") }
    var currentKmStr by remember { mutableStateOf("") }
    var nextKmStr by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }

    val clientsList by viewModel.clients.collectAsState()
    val maintenanceList by viewModel.maintenances.collectAsState()

    var showForm by remember { mutableStateOf(false) }

    // Check upcoming alerts
    val alertsList = remember(clientsList, maintenanceList) {
        clientsList.mapNotNull { client ->
            val newestMaint = maintenanceList.firstOrNull { it.clientPlate == client.plate }
            if (newestMaint != null) {
                val remaining = newestMaint.nextServiceKm - newestMaint.currentKm
                if (remaining <= 1500) {
                    Triple(client, newestMaint, remaining)
                } else null
            } else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mantenimiento y Kilometraje",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { showForm = !showForm },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showForm) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (showForm) "Cerrar" else "Nuevo Registro")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trigger Alerts banner
        if (alertsList.isNotEmpty()) {
            Text(
                text = "⚠️ Próximos Mantenimientos Alertas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = StateMalo,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 140.dp)
                    .background(Color(0xFFFFEBEE), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alertsList) { (client, maint, remaining) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${client.name} - ${client.vehicle}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TextDark
                                )
                                Text(
                                    "Patente: ${client.plate} | Programado: ${maint.nextServiceKm} KM",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Surface(
                                color = if (remaining <= 0) StateMalo else StateRegular,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (remaining <= 0) "VENCIDO" else "ENTREGA PRONTO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Form registration
        AnimatedVisibility(
            visible = showForm,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cargar Historial de Servicio o KMs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = plate,
                        onValueChange = { plate = it },
                        label = { Text("Patente del vehículo registrado") },
                        placeholder = { Text("ej. AE123XX") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maintenance_plate_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = currentKmStr,
                            onValueChange = { currentKmStr = it },
                            label = { Text("KM Ingreso") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("current_km_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        OutlinedTextField(
                            value = nextKmStr,
                            onValueChange = { nextKmStr = it },
                            label = { Text("Próximo Control (KM)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("next_km_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = observation,
                        onValueChange = { observation = it },
                        label = { Text("Observación del mantenimiento (ej. Cambio de aceite 10W40)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("maintenance_obs_input"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val currKm = currentKmStr.toIntOrNull() ?: 0
                            val nextKm = nextKmStr.toIntOrNull() ?: 0
                            if (plate.isBlank() || currKm <= 0 || nextKm <= 0) {
                                viewModel.showMessage("Verifique los valores ingresados. Todos los números deben ser mayores a cero.")
                                return@Button
                            }
                            viewModel.addMaintenance(plate, currKm, nextKm, observation) {
                                plate = ""
                                currentKmStr = ""
                                nextKmStr = ""
                                observation = ""
                                showForm = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_maintenance_button")
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Historial y Control KMs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = "Historial Técnico Reciente",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (maintenanceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No se registraron mantenimientos de kilometraje aún.",
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(maintenanceList) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Patente: ${record.clientPlate}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    record.datePerformed,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column {
                                    Text("KMs Ingresados", fontSize = 10.sp, color = Color.Gray)
                                    Text("${record.currentKm} KM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Próximo Servicio", fontSize = 10.sp, color = Color.Gray)
                                    Text("${record.nextServiceKm} KM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            if (record.observation.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Obs: ${record.observation}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ------------------- COMPONENTS TAB (CATALOGO) -------------------
@Composable
fun ComponentsScreen(viewModel: WorkshopViewModel) {
    var compName by remember { mutableStateOf("") }
    var isLevelCheck by remember { mutableStateOf(false) }

    val componentsList by viewModel.components.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Elementos de Chequeo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Catálogo general de inspección vehicular",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Form to add component
        AnimatedVisibility(
            visible = showAddForm,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Crear Nuevo Componente de Inspección",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = compName,
                        onValueChange = { compName = it },
                        label = { Text("Nombre del componente (ej. Correa alternador)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_component_name"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isLevelCheck,
                            onCheckedChange = { isLevelCheck = it },
                            modifier = Modifier.testTag("new_component_level_check")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Revisión de Nivel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Indica si se revisará nivel de fluido (A Nivel / Bajo Nivel)", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.addComponent(compName, isLevelCheck) {
                                compName = ""
                                isLevelCheck = false
                                showAddForm = false
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_component_button")
                    ) {
                        Text("Crear y Añadir a Inspecciones", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Standard Check components
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Filtros & Repuestos",
                        modifier = Modifier.padding(10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                
                val standardList = componentsList.filter { !it.isLevelCheck }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(standardList) { item ->
                        CatalogItemRow(item, onDelete = { viewModel.deleteComponent(item) })
                    }
                }
            }

            // Level check components
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Niveles de Fluidos",
                        modifier = Modifier.padding(10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                val levelList = componentsList.filter { item -> item.isLevelCheck }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(levelList) { item ->
                        CatalogItemRow(item, onDelete = { viewModel.deleteComponent(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItemRow(item: Component, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.name,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(18.dp)
                    .testTag("delete_component_${item.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}


// ------------------- CHEQUEO GENERAL TAB -------------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CheckupScreen(
    viewModel: WorkshopViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val matchedClient = viewModel.matchedClientForCheck
    val componentsList by viewModel.components.collectAsState()
    val maintenanceList by viewModel.maintenances.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Chequeo Técnico Express",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Diagnóstico de componentes activos y generación de certificado PDF",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // License Plate Search Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.queryPlate,
                onValueChange = { viewModel.queryPlate = it },
                label = { Text("Filtrar por Patente del Vehículo") },
                placeholder = { Text("ej. AE123XX") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("inspection_search_plate_input"),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.searchVehicleForCheck() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("inspection_search_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cargar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (matchedClient == null) {
            // Prompt to select client
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Garage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Sin vehículo de chequeo cargado.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Ingresa la patente arriba y haz click en 'Cargar', o registra un cliente nuevo si aún no existe.",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { onNavigateToTab(0) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ir a Clientes")
                        }
                    }
                }
            }
        } else {
            // Render inspection details - Premium Bento vehicle block!
            val latestMaintForPlate = remember(matchedClient.plate, maintenanceList) {
                maintenanceList.firstOrNull { it.clientPlate == matchedClient.plate }
            }
            val mileageStr = latestMaintForPlate?.let { "${it.currentKm} Km" } ?: "125.400 Km"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(BentoPrimary, Color(0xFF4A3485))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = matchedClient.plate.uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color.White,
                                letterSpacing = 2.sp
                            )
                            Surface(
                                color = Color(0xFFE8DEF8),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "ACTIVO",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF21005D),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${matchedClient.vehicle} • ${matchedClient.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = mileageStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Estado de Componentes:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(componentsList) { comp ->
                    CheckupItemRow(
                        component = comp,
                        currentStatus = viewModel.componentCheckStatuses[comp.id] ?: "",
                        currentObservation = viewModel.componentCheckObservations[comp.id] ?: "",
                        onStatusChanged = { stat -> viewModel.setComponentStatus(comp.id, stat) },
                        onObservationChanged = { obs -> viewModel.setComponentObservation(comp.id, obs) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = viewModel.generalMechanicObservation,
                        onValueChange = { viewModel.generalMechanicObservation = it },
                        label = { Text("Observaciones Generales del Diagnóstico (Mecánico)") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("general_inspection_notes"),
                        maxLines = 4,
                        minLines = 2
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveCheckupAndShare(context) { file ->
                                // Success flow
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StateBueno),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_inspection_pdf_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FINALIZAR CHEQUEO Y ENVIAR PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CheckupItemRow(
    component: Component,
    currentStatus: String,
    currentObservation: String,
    onStatusChanged: (String) -> Unit,
    onObservationChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("check_row_${component.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = component.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                if (component.isLevelCheck) {
                    val isChecked = currentStatus == "A Nivel"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onStatusChanged(if (isChecked) "Bajo Nivel" else "A Nivel") }
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onStatusChanged(if (it) "A Nivel" else "Bajo Nivel") },
                            modifier = Modifier.testTag("checkbox_level_${component.id}")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isChecked) "✔ A Nivel" else "✘ Bajo Nivel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) StateBueno else StateMalo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Standard buttons status (Good/Regular/Bad)
            if (!component.isLevelCheck) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BUENO button
                    val isBueno = currentStatus == "Bueno"
                    Button(
                        onClick = { onStatusChanged("Bueno") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_bueno_${component.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBueno) StateBueno else Color.White,
                            contentColor = if (isBueno) Color.White else StateBueno
                        ),
                        border = BorderStroke(1.dp, StateBueno),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Bueno", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // REGULAR button
                    val isRegular = currentStatus == "Regular"
                    Button(
                        onClick = { onStatusChanged("Regular") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_regular_${component.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRegular) StateRegular else Color.White,
                            contentColor = if (isRegular) Color.White else StateRegular
                        ),
                        border = BorderStroke(1.dp, StateRegular),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Regular", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // MALO button
                    val isMalo = currentStatus == "Malo"
                    Button(
                        onClick = { onStatusChanged("Malo") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_malo_${component.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMalo) StateMalo else Color.White,
                            contentColor = if (isMalo) Color.White else StateMalo
                        ),
                        border = BorderStroke(1.dp, StateMalo),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Malo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Small observation field
            OutlinedTextField(
                value = currentObservation,
                onValueChange = onObservationChanged,
                label = { Text("Nota o hallazgo del componente", fontSize = 11.sp) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_${component.id}"),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
            )
        }
    }
}
