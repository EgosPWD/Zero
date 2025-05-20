package com.example.zero.views.plant

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.remote.RetrofitClient
import com.example.zero.data.remote.plant.PlantRepository
import com.example.zero.domain.Plant
import com.example.zero.utils.StorageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream

data class AddPlantUiState(
    val plantName: String = "",
    val plantDescription: String = "",
    val isIdentifying: Boolean = false,
    val isGeneratingDescription: Boolean = false,
    val isLoading: Boolean = false,
    val isPlantCreated: Boolean = false,
    val error: String? = null
)

class AddPlantViewModel(
    private val plantRepository: PlantRepository = PlantRepository(),
    private val storageUtils: StorageUtils = StorageUtils()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPlantUiState())
    val uiState: StateFlow<AddPlantUiState> = _uiState.asStateFlow()

    fun updatePlantName(name: String) {
        _uiState.update { it.copy(plantName = name) }
    }

    fun updatePlantDescription(description: String) {
        _uiState.update { it.copy(plantDescription = description) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Description API setup
    private val descriptionRetrofit = Retrofit.Builder()
        .baseUrl("https://backlord.prod.dtt.tja.ucb.edu.bo/") // Using the same base URL as plant API
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val descriptionApiService = descriptionRetrofit.create(DescriptionApiService::class.java)

    fun identifyPlant(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isIdentifying = true, error = null) }

                // Convert Uri to File
                val file = uriToFile(imageUri, context)

                // Create multipart request
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
                val organsPart = MultipartBody.Part.createFormData("organs", "auto")

                // Call the API using RetrofitClient
                val response = RetrofitClient.api.identifyPlant(imagePart, organsPart)

                if (response.isSuccessful && response.body() != null) {
                    val plantName = response.body()?.plantName ?: "Planta desconocida"
                    _uiState.update { it.copy(plantName = plantName) }

                    // Now generate a description for the identified plant
                    generatePlantDescription(plantName)
                } else {
                    _uiState.update {
                        it.copy(
                            error = "No se pudo identificar la planta. Intente nuevamente o ingrese el nombre manualmente."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al identificar planta: ${e.message}"
                    )
                }
            } finally {
                _uiState.update { it.copy(isIdentifying = false) }
            }
        }
    }

    private fun generatePlantDescription(plantName: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isGeneratingDescription = true) }

                try {
                    // Call the real API endpoint for description
                    val response = descriptionApiService.getPlantDescription(plantName)

                    if (response.isSuccessful && response.body() != null) {
                        val description = response.body()?.description ?: ""
                        _uiState.update { it.copy(plantDescription = description) }
                    } else {
                        // If API call fails, use a default description
                        val fallbackDescription = "Esta es una ${plantName}. " +
                                "Es una planta que requiere cuidados regulares y exposición adecuada al sol. " +
                                "Consulta guías específicas para obtener más información sobre su cuidado."
                        _uiState.update { it.copy(plantDescription = fallbackDescription) }
                    }
                } catch (e: Exception) {
                    // If any error occurs, use the fallback description
                    val fallbackDescription = "Esta es una ${plantName}. " +
                            "Es una planta común que requiere luz solar moderada y riego regular. " +
                            "Típicamente crece mejor en suelos bien drenados."
                    _uiState.update { it.copy(plantDescription = fallbackDescription) }
                }
            } finally {
                _uiState.update { it.copy(isGeneratingDescription = false) }
            }
        }
    }

    fun addPlant(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // 1. Upload image to Firebase Storage
                val imageUrl = storageUtils.uploadImage(imageUri)

                // 2. Create Plant object
                val plant = Plant(
                    name = uiState.value.plantName,
                    description = uiState.value.plantDescription,
                    imageUrl = imageUrl
                )

                // 3. Add plant to Firestore
                plantRepository.addPlant(plant)

                // 4. Update state to indicate success
                _uiState.update { it.copy(isPlantCreated = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Error al guardar la planta: ${e.message}"
                    )
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("plant_image", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
}
