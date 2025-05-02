package com.example.zero.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PlantApiService {
    @Multipart
    @POST("identify/")
    suspend fun identifyPlant(
        @Part image: MultipartBody.Part,
        @Part organs: MultipartBody.Part
    ): Response<PlantResponse>
}

@JsonClass(generateAdapter = true)
data class PlantResponse(
    @Json(name = "plant_name") val plantName: String?
)
