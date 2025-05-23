package com.example.zero.views.camera

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.zero.R
import com.example.zero.data.remote.plant.PlantDiagnosisResponse
import com.example.zero.views.components.ActionButton

@Composable
fun CameraScreenContent(
    imageUri: Uri?,
    plantName: String?,
    errorMessage: String?,
    diagnosisResult: PlantDiagnosisResponse?,
    isLoading: Boolean,
    isDiagnosing: Boolean,
    showDiagnosis: Boolean,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onIdentifyClick: () -> Unit,
    onDiagnoseClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de la sección
            Text(
                text = "Identificador de Plantas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Previsualización de imagen mejorada con bordes redondeados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen de planta",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.log),
                            error = painterResource(id = R.drawable.log)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Sin imagen seleccionada",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Utiliza los botones para seleccionar una imagen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar resultado de identificación o diagnóstico
            if (!showDiagnosis) {
                PlantIdentificationResult(
                    plantName = plantName,
                    errorMessage = errorMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PlantDiagnosisResult(
                    diagnosisResult = diagnosisResult,
                    errorMessage = errorMessage,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recomendación
            if (diagnosisResult != null && !diagnosisResult.salud.esSaludable && diagnosisResult.enfermedades.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Recomendaciones",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f)
                        )

                        Text(
                            text = "1. Consulta con un experto en jardinería para confirmar el diagnóstico.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "2. Mantén la planta en un ambiente adecuado con luz y riego apropiados.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text(
                            text = "3. Considera utilizar tratamientos orgánicos específicos para la enfermedad detectada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botones en una fila para mejor accesibilidad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Cámara",
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isDiagnosing
                )

                ActionButton(
                    text = "Galería",
                    onClick = onGalleryClick,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isDiagnosing
                )
            }

            // Botones de identificación y diagnóstico
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de identificación
                ActionButton(
                    text = if (isLoading) "Identificando..." else "Identificar",
                    onClick = onIdentifyClick,
                    enabled = !isLoading && !isDiagnosing && imageUri != null,
                    modifier = Modifier.weight(1f)
                )

                // Botón de diagnóstico
                ActionButton(
                    text = if (isDiagnosing) "Diagnosticando..." else "Diagnosticar",
                    onClick = onDiagnoseClick,
                    enabled = !isLoading && !isDiagnosing && imageUri != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Indicador de carga
        if (isLoading || isDiagnosing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 5.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isLoading) "Identificando planta..." else "Diagnosticando problemas...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
