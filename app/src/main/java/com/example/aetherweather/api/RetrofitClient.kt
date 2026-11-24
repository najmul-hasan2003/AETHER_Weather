package com.example.aetherweather.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    private val okHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    // Weather API
    private val weatherRetrofit by lazy {
        createRetrofit(
            "https://api.open-meteo.com/"
        )
    }

    // Geocoding API
    private val geocodingRetrofit by lazy {
        createRetrofit(
            "https://geocoding-api.open-meteo.com/"
        )
    }

    // Air Quality API
    private val airQualityRetrofit by lazy {
        createRetrofit(
            "https://air-quality-api.open-meteo.com/"
        )
    }

    val weatherApi: WeatherApiService by lazy {
        weatherRetrofit.create(
            WeatherApiService::class.java
        )
    }

    val geocodingApi: GeocodingApiService by lazy {
        geocodingRetrofit.create(
            GeocodingApiService::class.java
        )
    }

    val airQualityApi: AirQualityApiService by lazy {
        airQualityRetrofit.create(
            AirQualityApiService::class.java
        )
    }
}
