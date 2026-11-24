package com.example.aetherweather.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather
)

data class CurrentWeather(

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("apparent_temperature")
    val feelsLike: Double,

    @SerializedName("is_day")
    val isDay: Int,

    val precipitation: Double,

    @SerializedName("weather_code")
    val weatherCode: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double
)

data class HourlyWeather(

    val time: List<String>,

    @SerializedName("temperature_2m")
    val temperature: List<Double>,

    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>,

    @SerializedName("weather_code")
    val weatherCode: List<Int>
)

data class DailyWeather(

    val time: List<String>,

    @SerializedName("weather_code")
    val weatherCode: List<Int>,

    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,

    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,

    @SerializedName("uv_index_max")
    val uvIndexMax: List<Double>,

    // Sunrise Time
    val sunrise: List<String>,

    // Sunset Time
    val sunset: List<String>
)
