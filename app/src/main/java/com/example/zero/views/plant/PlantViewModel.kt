package com.example.zero.views.plant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zero.data.remote.plant.PlantRepository
import com.example.zero.domain.Plant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlantViewModel(
    private val plantRepository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _shouldNavigateToAddPlant = MutableStateFlow(false)
    val shouldNavigateToAddPlant: StateFlow<Boolean> = _shouldNavigateToAddPlant.asStateFlow()

    init {
        loadPlants()
    }

    fun loadPlants() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val plantsList = plantRepository.getplants()
                _plants.value = plantsList
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateToAddPlant() {
        _shouldNavigateToAddPlant.value = true
    }

    fun onAddPlantNavigated() {
        _shouldNavigateToAddPlant.value = false
    }
}
