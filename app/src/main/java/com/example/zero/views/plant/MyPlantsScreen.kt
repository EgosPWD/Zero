package com.example.zero.views.plant

import WeatherResponse
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.navigation.NavController
import com.example.zero.domain.Plant


@Composable
fun MyPlantsScreen(
    navController: NavController,
    viewModel: PlantViewModel = viewModel()
) {
    val plantList by viewModel.plants.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val shouldNavigateToAddPlant by viewModel.shouldNavigateToAddPlant.collectAsState()
    val deletionStatus by viewModel.deletionStatus.collectAsState()
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var plantToDelete by remember { mutableStateOf<Plant?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val apiKey = "2f06aaf89c0647c5844191401250205"

    // Handle navigation to AddPlantScreen
    LaunchedEffect(shouldNavigateToAddPlant) {
        if (shouldNavigateToAddPlant) {
            navController.navigate("add_plant")
            viewModel.onAddPlantNavigated()
        }
    }

    // Refresh plant list when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.loadPlants()
    }

    // Obtener plantas y clima al mismo tiempo
    LaunchedEffect(Unit) {
        try {
            val weatherApi = WeatherApi.create()
            weather = weatherApi.getCurrentWeather(apiKey, "Tarija")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Mostrar Snackbar cuando se completa una eliminación
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(deletionStatus) {
        when (deletionStatus) {
            is DeletionStatus.Success -> {
                snackbarHostState.showSnackbar("Planta eliminada con éxito")
                viewModel.resetDeletionStatus()
            }
            is DeletionStatus.Error -> {
                snackbarHostState.showSnackbar("Error: ${(deletionStatus as DeletionStatus.Error).message}")
                viewModel.resetDeletionStatus()
            }
            else -> {}
        }
    }

    // Diálogo de confirmación de eliminación
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar planta?") },
            text = { Text("¿Estás seguro que deseas eliminar esta planta? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        plantToDelete?.id?.let {
                            viewModel.deletePlant(it)
                        }
                        showDeleteDialog = false
                        plantToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        plantToDelete = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateToAddPlant() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Añadir planta",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Clima en tu ciudad",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (weather != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Ciudad: ${weather!!.location.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Clima: ${weather!!.current.condition.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Temp: ${weather!!.current.temp_c}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        AsyncImage(
                            model = "https:${weather!!.current.condition.icon}",
                            contentDescription = "Icono del clima",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            } else {
                Text(
                    "Cargando clima...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Tus Plantas",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LazyColumn {
                    items(plantList) { plant ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (plant.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = plant.imageUrl,
                                        contentDescription = plant.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = plant.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = {
                                            plantToDelete = plant
                                            showDeleteDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar planta",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                Text(
                                    text = plant.description,
                                    style = MaterialTheme.typography.bodyMedium,
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
