package com.example.zero.data.remote.plant

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlantDiagnosisResponse(
    val enfermedades: List<Enfermedad>,
    val salud: Salud,
    @Json(name = "es_planta") val esPlanta: EsPlanta
)

@JsonClass(generateAdapter = true)
data class Enfermedad(
    val name: String,
    val probability: Double
)

@JsonClass(generateAdapter = true)
data class Salud(
    val probabilidad: Double,
    @Json(name = "umbral_saludable") val umbralSaludable: Double,
    @Json(name = "es_saludable") val esSaludable: Boolean
)

@JsonClass(generateAdapter = true)
data class EsPlanta(
    val probabilidad: Double,
    val confirmado: Boolean
)
