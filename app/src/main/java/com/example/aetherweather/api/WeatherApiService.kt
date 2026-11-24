package com.example.aetherweather.api

import com.example.aetherweather.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeather(

        @Query("latitude")
        latitude: Double,

        @Query("longitude")
        longitude: Double,

        @Query("current")
        current: String =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,wind_speed_10m",

        @Query("hourly")
        hourly: String =
            "temperature_2m,precipitation_probability,weather_code",

        @Query("daily")
        daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,sunrise,sunset",

        @Query("timezone")
        timezone: String = "auto",

        @Query("forecast_days")
        forecastDays: Int = 7

    ): WeatherResponse
}
