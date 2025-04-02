package com.example.zero.views.camera

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.zero.utils.CameraUtils

@Composable
fun CameraScreen() {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) {
            Toast.makeText(context, "Error al capturar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Captured Image",
                modifier = Modifier.size(200.dp)
            )
        }

        Button(onClick = {
            val uri = CameraUtils.createImageUri(context)
            if (uri != null) {
                imageUri = uri
                cameraLauncher.launch(uri)
            }
        }) {
            Text("Abrir Cámara")
        }
    }
}