package com.example.zero.views.plant

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DescriptionApiService {
    @GET("api/v1/plant-description")
    suspend fun getPlantDescription(
        @Query("plant_name") plantName: String
    ): Response<PlantDescriptionResponse>
}

data class PlantDescriptionResponse(
    val description: String
)
