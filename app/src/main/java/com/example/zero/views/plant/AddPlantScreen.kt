package com.example.zero.views.plant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.zero.domain.Plant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    navController: NavController,
    viewModel: AddPlantViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            viewModel.identifyPlant(uri, context)
        }
    }

    // Observe plant identification and creation results
    LaunchedEffect(uiState.isPlantCreated) {
        if (uiState.isPlantCreated) {
            snackbarHostState.showSnackbar("¡Planta añadida con éxito!")
            navController.popBackStack()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Añadir Planta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Image selector
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.33f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Imagen de la planta",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Añadir foto",
                            modifier = Modifier.height(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Toca para añadir una foto",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Plant name field - automatically filled after identification
            OutlinedTextField(
                value = uiState.plantName,
                onValueChange = { viewModel.updatePlantName(it) },
                label = { Text("Nombre de la planta") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isIdentifying
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description field - automatically filled or editable
            OutlinedTextField(
                value = uiState.plantDescription,
                onValueChange = { viewModel.updatePlantDescription(it) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                enabled = !uiState.isGeneratingDescription
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Add button
            Button(
                onClick = {
                    imageUri?.let {
                        viewModel.addPlant(it, context)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = imageUri != null && uiState.plantName.isNotEmpty() &&
                          !uiState.isLoading && !uiState.isIdentifying && !uiState.isGeneratingDescription
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.height(24.dp)
                    )
                } else {
                    Text("Añadir Planta")
                }
            }

            if (uiState.isIdentifying) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Identificando planta...")
                CircularProgressIndicator()
            }

            if (uiState.isGeneratingDescription) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Generando descripción...")
                CircularProgressIndicator()
            }
        }
    }
}
