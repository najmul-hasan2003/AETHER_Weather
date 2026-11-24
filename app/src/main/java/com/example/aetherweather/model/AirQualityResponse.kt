package com.example.aetherweather.model


import com.google.gson.annotations.SerializedName

data class AirQualityResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentAirQuality
)

data class CurrentAirQuality(

    val time: String?,

    @SerializedName("european_aqi")
    val europeanAqi: Int?,

    @SerializedName("us_aqi")
    val usAqi: Int?,

    @SerializedName("pm2_5")
    val pm25: Double?,

    val pm10: Double?,

    @SerializedName("carbon_monoxide")
    val carbonMonoxide: Double?,

    @SerializedName("nitrogen_dioxide")
    val nitrogenDioxide: Double?,

    @SerializedName("sulphur_dioxide")
    val sulphurDioxide: Double?,

    val ozone: Double?
)
