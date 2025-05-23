package com.example.zero.views.camera

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.zero.data.remote.RetrofitClient
import com.example.zero.data.remote.plant.PlantDiagnosisResponse
import com.example.zero.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "PlantAPIUtils"

/**
 * Identifica una planta a partir de su imagen
 */
suspend fun identifyPlantFromUri(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) = withContext(Dispatchers.IO) {
    var tempFile: File? = null
    try {
        tempFile = CameraUtils.getFileFromUri(context, uri)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
            onError("Error al procesar el archivo de imagen.")
            return@withContext
        }
        if (tempFile.length() > 10_000_000) {
            onError("La imagen es demasiado grande (máximo 10MB).")
            return@withContext
        }
        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
        val organsPart = MultipartBody.Part.createFormData("organs", "auto")

        val response = RetrofitClient.api.identifyPlant(imagePart, organsPart)
        if (response.isSuccessful) {
            val name = response.body()?.plantName
            if (!name.isNullOrBlank()) withContext(Dispatchers.Main) { onSuccess(name) }
            else withContext(Dispatchers.Main) { onError("No se pudo identificar la planta (respuesta vacía).") }
        } else {
            withContext(Dispatchers.Main) { onError("Error del servidor (${response.code()})") }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error en identificación: ${e.message}", e)
        withContext(Dispatchers.Main) { onError("Error de conexión o procesamiento: ${e.localizedMessage}") }
    } finally {
        tempFile?.delete()
    }
}

/**
 * Diagnostica enfermedades en una planta a partir de su imagen
 */
suspend fun diagnosePlantFromUri(
    context: Context,
    uri: Uri,
    onSuccess: (PlantDiagnosisResponse) -> Unit,
    onError: (String) -> Unit
) = withContext(Dispatchers.IO) {
    var tempFile: File? = null
    try {
        tempFile = CameraUtils.getFileFromUri(context, uri)
        if (tempFile == null || !tempFile.exists() || tempFile.length() == 0L) {
            withContext(Dispatchers.Main) { onError("Error al procesar el archivo de imagen.") }
            return@withContext
        }

        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        val organsPart = MultipartBody.Part.createFormData("organs", "auto")

        val response = RetrofitClient.api.diagnosePlant(imagePart, organsPart)
        if (response.isSuccessful) {
            response.body()?.let {
                withContext(Dispatchers.Main) { onSuccess(it) }
            } ?: withContext(Dispatchers.Main) { onError("No se pudo obtener un diagnóstico") }
        } else {
            Log.e(TAG, "Error en diagnóstico: ${response.code()} - ${response.errorBody()?.string()}")
            withContext(Dispatchers.Main) { onError("Error del servidor (${response.code()})") }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Excepción en diagnóstico: ${e.message}", e)
        withContext(Dispatchers.Main) { onError("Error: ${e.localizedMessage}") }
    } finally {
        tempFile?.delete()
    }
}
