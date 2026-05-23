package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.sp
import com.example.ui.CheckupScreen
import com.example.ui.ClientsScreen
import com.example.ui.ComponentsScreen
import com.example.ui.MaintenanceScreen
import com.example.ui.WorkshopViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WorkshopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppHost(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppHost(viewModel: WorkshopViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val currentMessage = viewModel.actionMessage

    // Observe message to show in snackbar
    LaunchedEffect(currentMessage) {
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(
                message = currentMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Clientes") },
                    label = { Text("Clientes", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_clients")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.QueryStats, contentDescription = "Alertas KMs") },
                    label = { Text("KMs Alertas", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_alerts")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Catálogo") },
                    label = { Text("Catálogo", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_catalog")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Chequear") },
                    label = { Text("Chequear", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_checkup")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ClientsScreen(
                    viewModel = viewModel,
                    onSelectPlateForInspection = { plate ->
                        // Automatically preload plate search and run lookup
                        viewModel.queryPlate = plate
                        viewModel.searchVehicleForCheck()
                        // Route tab
                        selectedTab = 3
                    }
                )
                1 -> MaintenanceScreen(viewModel = viewModel)
                2 -> ComponentsScreen(viewModel = viewModel)
                3 -> CheckupScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { target ->
                        selectedTab = target
                    }
                )
            }
        }
    }
}
