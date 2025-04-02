package com.example.zero.views.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.zero.data.remote.RetrofitClient
import com.example.zero.data.remote.PlantResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// Define TAG para logging
private const val TAG = "CameraScreen"

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var plantName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Estado para permisos
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }


    // Define the camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Image captured successfully, imageUri should already be set by the onClick
            Log.d(TAG, "Image captured successfully with URI: $imageUri")
            plantName = null // Reset previous result
            errorMessage = null // Reset previous error
        } else {
            Log.w(TAG, "Image capture failed or was cancelled.")
            // Only show toast if the URI was actually set before launching
            if (imageUri != null) {
                Toast.makeText(context, "Captura de imagen cancelada o fallida", Toast.LENGTH_SHORT).show()
                // Reset imageUri if capture failed after URI was set, so UI reflects it
                imageUri = null
            }
        }
    }

    // Define the gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.d(TAG, "Image selected from gallery: $it")
            imageUri = it
            plantName = null // Reset previous result
            errorMessage = null // Reset previous error
        } ?: Log.w(TAG, "No image selected from gallery.")
    }

    // Función para lanzar la cámara
    fun launchCamera(context: Context, onUriCreated: (Uri) -> Unit) {
        try {
            val uri = createImageFileAndGetUri(context)
            if (uri != null) {
                onUriCreated(uri)
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Error al preparar la cámara", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar la cámara", e)
            Toast.makeText(context, "No se pudo iniciar la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Image or Placeholder
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize() // Fill the Box
                )
            } else {
                Text(
                    "No hay imagen seleccionada",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Button(
            onClick = {
                plantName = null
                errorMessage = null

                if (!hasCameraPermission) {
                } else {
                    launchCamera(context) { uri ->
                        imageUri = uri
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Abrir Cámara")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                plantName = null
                errorMessage = null
                Log.d(TAG, "Launching gallery picker.")
                galleryLauncher.launch("image/*")
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Seleccionar Imagen de la Galería")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val currentUri = imageUri
                currentUri?.let { uri ->
                    if (isLoading) return@Button

                    isLoading = true
                    plantName = null
                    errorMessage = null

                    Log.d(TAG, "Starting plant identification for URI: $uri")
                    coroutineScope.launch {
                        var tempFile: File? = null
                        try {
                            tempFile = getFileFromUri(context, uri)
                            if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
                                Log.e(TAG, "Invalid file obtained from URI: $uri. File: $tempFile, Exists: ${tempFile?.exists()}, Length: ${tempFile?.length()}")
                                if (errorMessage == null) {
                                    errorMessage = "Error al procesar el archivo de imagen."
                                }
                                isLoading = false
                                return@launch
                            }

                            if (tempFile.length() > 10_000_000) {
                                Log.e(TAG, "Image too large: ${tempFile.length()} bytes")
                                errorMessage = "La imagen es demasiado grande (máximo 10MB)."
                                isLoading = false
                                return@launch
                            }

                            Log.d(TAG, "Preparing to upload file: ${tempFile.name}, Size: ${tempFile.length()} bytes")

                            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                            val organsPart = MultipartBody.Part.createFormData("organs", "auto")

                            Log.d(TAG, "Sending request to identifyPlant endpoint...")
                            val response = RetrofitClient.api.identifyPlant(imagePart, organsPart)

                            if (response.isSuccessful) {
                                Log.d(TAG, "API call successful. Code: ${response.code()}")
                                val responseBody: PlantResponse? = response.body()
                                if (responseBody != null) {
                                    Log.d(TAG, "Response body received: $responseBody")
                                    if (!responseBody.plantName.isNullOrBlank()) {
                                        plantName = responseBody.plantName
                                        Log.i(TAG, "Plant identified successfully: $plantName")
                                    } else {
                                        Log.w(TAG, "Plant identification successful but plantName is null or empty in response: $responseBody")
                                        errorMessage = "No se pudo identificar la planta (respuesta vacía)."
                                    }
                                } else {
                                    Log.w(TAG, "API call successful but response body is null or could not be parsed.")
                                    errorMessage = "Respuesta del servidor inesperada o vacía."
                                }
                            } else {
                                val errorBody = response.errorBody()?.string() ?: "No error body available"
                                Log.e(TAG, "API call failed. Code: ${response.code()}, Message: ${response.message()}, Error Body: $errorBody")
                                errorMessage = when(response.code()){
                                    400 -> "Solicitud incorrecta. Verifique la imagen."
                                    404 -> "Servicio de identificación no encontrado."
                                    500 -> "Error interno del servidor de identificación."
                                    else -> "Error del servidor (${response.code()})."
                                }
                            }

                        } catch (e: Exception) {
                            Log.e(TAG, "Exception during plant identification API call or file processing", e)
                            errorMessage = "Error de conexión o procesamiento: ${e.localizedMessage ?: "Error desconocido"}"
                        } finally {
                            isLoading = false
                            Log.d(TAG, "Identification process finished. isLoading set to false.")
                            tempFile?.let {
                                if (it.exists()) {
                                    val deleted = it.delete()
                                    Log.d(TAG, "Temporary file ${it.name} deletion attempt successful: $deleted")
                                }
                            }
                        }
                    }
                } ?: run {
                    Log.w(TAG, "Identify button clicked but imageUri is null.")
                    Toast.makeText(context, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            enabled = !isLoading && imageUri != null
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identificando...")
                }
            } else {
                Text("Identificar Planta")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.heightIn(min = 40.dp), contentAlignment = Alignment.Center) {
            when {
                plantName != null -> {
                    Text(
                        "Planta Identificada: $plantName",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                errorMessage != null -> {
                    Text(
                        "Error: $errorMessage",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

private fun getFileFromUri(context: Context, uri: Uri): File? {
    val fileName = "upload_image_${System.currentTimeMillis()}.jpg"
    val file = File(context.cacheDir, fileName)

    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: run {
            Log.e(TAG, "ContentResolver failed to open InputStream for URI: $uri")
            return null
        }

        if (!file.exists() || file.length() == 0L) {
            Log.e(TAG, "Failed to copy data or file is empty. File: ${file.absolutePath}")
            if(file.exists()) file.delete()
            return null
        }

        return file
    } catch (e: Exception) {
        Log.e(TAG, "Exception getting file from URI: $uri", e)
        if(file.exists()) file.delete()
        return null
    }
}

private fun createImageFileAndGetUri(context: Context): Uri? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"

    try {
        val storageDir = File(context.cacheDir, "camera_photos")
        if (!storageDir.exists()) storageDir.mkdirs()

        val imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(
            context,
            "com.example.zero.fileprovider", // Asegúrate de que coincida con tu AndroidManifest
            imageFile
        )
    } catch (e: IOException) {
        Log.e(TAG, "Error creating image file", e)
        return null
    } catch (e: Exception) {
        Log.e(TAG, "Error creating URI for image file", e)
        return null
    }
}