package com.example.zero.views.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.launch
import com.example.zero.utils.CameraUtils.createImageUri
import com.example.zero.views.components.ActionButton

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
    var diagnosisResult by remember { mutableStateOf<com.example.zero.data.remote.plant.PlantDiagnosisResponse?>(null) }
    var isDiagnosing by remember { mutableStateOf(false) }
    var showDiagnosis by remember { mutableStateOf(false) }
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
            diagnosisResult = null
            showDiagnosis = false
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
            diagnosisResult = null
            showDiagnosis = false
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

    CameraScreenContent(
        imageUri = imageUri,
        plantName = plantName,
        errorMessage = errorMessage,
        diagnosisResult = diagnosisResult,
        isLoading = isLoading,
        isDiagnosing = isDiagnosing,
        showDiagnosis = showDiagnosis,
        onCameraClick = {
            plantName = null
            errorMessage = null
            diagnosisResult = null
            showDiagnosis = false
            if (!hasCameraPermission) {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                launchCamera()
            }
        },
        onGalleryClick = {
            plantName = null
            errorMessage = null
            diagnosisResult = null
            showDiagnosis = false
            if (!hasStoragePermission) {
                storagePermissionLauncher.launch(requiredStoragePermission)
            } else {
                galleryLauncher.launch("image/*")
            }
        },
        onIdentifyClick = {
            imageUri?.let { uri ->
                isLoading = true
                plantName = null
                errorMessage = null
                diagnosisResult = null
                showDiagnosis = false
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
        onDiagnoseClick = {
            imageUri?.let { uri ->
                isDiagnosing = true
                diagnosisResult = null
                errorMessage = null
                showDiagnosis = true
                coroutineScope.launch {
                    diagnosePlantFromUri(
                        context = context,
                        uri = uri,
                        onSuccess = { diagnosisResult = it },
                        onError = { errorMessage = it }
                    )
                    isDiagnosing = false
                }
            } ?: Toast.makeText(context, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show()
        }
    )
}
