package com.example.aetherweather.model


data class GeocodingResponse(
    val results: List<CityResult>?
)

data class CityResult(
    val id: Int?,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val admin1: String?
)
