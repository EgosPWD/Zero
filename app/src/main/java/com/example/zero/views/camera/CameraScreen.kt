package com.example.zero.views.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.zero.data.remote.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*
import com.example.zero.utils.CameraUtils.createImageUri
import com.example.zero.utils.CameraUtils.getFileFromUri
import com.example.zero.views.components.ImagePreview
import com.example.zero.views.components.ActionButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
private const val TAG = "CameraScreen"

@Composable
fun CameraScreen() {
    val context = LocalContext.current

    val requiredStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredStoragePermission) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    var lastPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var plantName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) {
            Log.e(TAG, "galleryLauncher: returned null uri")
        } else {
            lastPhotoUri = uri
            imageUri = uri
            plantName = null
            errorMessage = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasStoragePermission = granted
        if (granted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = lastPhotoUri
            plantName = null
            errorMessage = null
        } else {
            lastPhotoUri = null
            imageUri = null
            Toast.makeText(context, "Captura cancelada o fallida", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera() {
        try {
            val uri = createImageUri(context)
            if (uri != null) {
                lastPhotoUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Error al preparar la cámara", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo iniciar la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            // Previsualización de imagen más grande y con bordes redondeados
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(horizontal = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentScale = ContentScale.Crop
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
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resultado de identificación mostrado antes de los botones
            AnimatedVisibility(
                visible = plantName != null || errorMessage != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = if (plantName != null) MaterialTheme.colorScheme.primaryContainer
                                   else MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        plantName != null -> Text(
                            "Planta Identificada: $plantName",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        errorMessage != null -> Text(
                            "Error: $errorMessage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    onClick = {
                        plantName = null
                        errorMessage = null
                        if (!hasCameraPermission) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            launchCamera()
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    text = "Galería",
                    onClick = {
                        plantName = null
                        errorMessage = null
                        if (!hasStoragePermission) {
                            storagePermissionLauncher.launch(requiredStoragePermission)
                        } else {
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.weight(1f)                )

            }

            // Botón de identificación más grande y destacado
            ActionButton(
                text = if (isLoading) "Identificando..." else "Identificar Planta",
                onClick = {
                    imageUri?.let { uri ->
                        isLoading = true
                        plantName = null
                        errorMessage = null
                        coroutineScope.launch {
                            identifyPlantFromUri(
                                context = context,
                                uri = uri,
                                onSuccess = { plantName = it },
                                onError = { errorMessage = it }
                            )
                            isLoading = false
                        }
                    } ?: Toast.makeText(context, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
                },
                enabled = !isLoading && imageUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        // Indicador de carga
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

suspend fun identifyPlantFromUri(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    var tempFile: File? = null
    try {
        tempFile = getFileFromUri(context, uri)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
            onError("Error al procesar el archivo de imagen.")
            return
        }
        if (tempFile.length() > 10_000_000) {
            onError("La imagen es demasiado grande (máximo 10MB).")
            return
        }
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        val organsPart = MultipartBody.Part.createFormData("organs", "auto")
        val response = RetrofitClient.api.identifyPlant(imagePart, organsPart)
        if (response.isSuccessful) {
            val name = response.body()?.plantName
            if (!name.isNullOrBlank()) onSuccess(name)
            else onError("No se pudo identificar la planta (respuesta vacía).")
        } else {
            onError("Error del servidor (${response.code()})")
        }
    } catch (e: Exception) {
        onError("Error de conexión o procesamiento: ${e.localizedMessage}")
    } finally {
        tempFile?.delete()
    }
}

