package com.example.aetherweather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieDrawable
import com.example.aetherweather.adapter.DailyAdapter
import com.example.aetherweather.adapter.DailyItem
import com.example.aetherweather.adapter.HourlyAdapter
import com.example.aetherweather.adapter.HourlyItem
import com.example.aetherweather.api.RetrofitClient
import com.example.aetherweather.databinding.ActivityMainBinding
import com.example.aetherweather.model.AirQualityResponse
import com.example.aetherweather.model.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isCelsius = true
    private var latestWeather: WeatherResponse? = null
    private var currentAnimationFile: String? = null

    private val preferences by lazy {
        getSharedPreferences("AETHER_SETTINGS", MODE_PRIVATE)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                fetchCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        applySavedTheme()

        super.onCreate(savedInstanceState)

        isCelsius = preferences.getBoolean("is_celsius", true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerViews()
        setupClickListeners()

        binding.tvLocation.text = "Dhaka, Bangladesh"

        loadWeather(
            23.8103,
            90.4125,
            "Dhaka, Bangladesh"
        )
    }

    private fun applySavedTheme() {

        val themeMode = preferences.getString("theme_mode", "dark")

        val nightMode = when (themeMode) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun setupClickListeners() {

        binding.tvSettings.setOnClickListener {
            startActivity(
                Intent(this, SettingsActivity::class.java)
            )
        }

        binding.tvLocation.setOnClickListener {
            checkLocationPermission()
        }

        binding.tvSearch.setOnClickListener {
            searchCity()
        }

        binding.etCity.setOnEditorActionListener { _, actionId, _ ->

            if (
                actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {

                searchCity()

                val imm = getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

                imm.hideSoftInputFromWindow(
                    binding.etCity.windowToken,
                    0
                )

                true
            } else {
                false
            }
        }

        binding.tvRefresh.setOnClickListener {

            binding.etCity.text?.clear()

            loadWeather(
                23.8103,
                90.4125,
                "Dhaka, Bangladesh"
            )
        }

        binding.tvUnitToggle.setOnClickListener {

            isCelsius = !isCelsius

            preferences.edit()
                .putBoolean("is_celsius", isCelsius)
                .apply()

            latestWeather?.let { weather ->
                showWeather(weather)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!::binding.isInitialized) return

        val savedUnit = preferences.getBoolean("is_celsius", true)

        if (savedUnit != isCelsius) {

            isCelsius = savedUnit

            latestWeather?.let { weather ->
                showWeather(weather)
            }
        }
    }

    // Setup RecyclerViews
    private fun setupRecyclerViews() {

        binding.rvHourly.layoutManager =
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.rvDaily.layoutManager =
            LinearLayoutManager(this)

        binding.rvHourly.isNestedScrollingEnabled = false
        binding.rvDaily.isNestedScrollingEnabled = false
    }

    // Check Location Permission
    private fun checkLocationPermission() {

        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (
            finePermission == PackageManager.PERMISSION_GRANTED ||
            coarsePermission == PackageManager.PERMISSION_GRANTED
        ) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Fetch Current Location
    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation() {

        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = "Getting current location..."

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->

            if (location != null) {

                lifecycleScope.launch {

                    val locationName = withContext(Dispatchers.IO) {

                        try {

                            val geocoder = Geocoder(
                                this@MainActivity,
                                Locale.getDefault()
                            )

                            @Suppress("DEPRECATION")
                            val address = geocoder.getFromLocation(
                                location.latitude,
                                location.longitude,
                                1
                            )?.firstOrNull()

                            val city = address?.locality
                                ?: address?.subAdminArea
                                ?: address?.adminArea

                            val country = address?.countryName

                            when {
                                !city.isNullOrBlank() &&
                                        !country.isNullOrBlank() ->
                                    "$city, $country"

                                !city.isNullOrBlank() -> city

                                else -> "Current Location"
                            }

                        } catch (e: Exception) {
                            "Current Location"
                        }
                    }

                    loadWeather(
                        location.latitude,
                        location.longitude,
                        locationName
                    )
                }

            } else {

                binding.tvStatus.text = "Unable to get location"

                Toast.makeText(
                    this,
                    "Please enable your device location",
                    Toast.LENGTH_LONG
                ).show()
            }

        }.addOnFailureListener { exception ->

            binding.tvStatus.text = "Location failed"

            Toast.makeText(
                this,
                "Location error: ${exception.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Search City
    private fun searchCity() {

        val cityName = binding.etCity.text.toString().trim()

        if (cityName.isEmpty()) {
            binding.etCity.error = "Enter a city name"
            return
        }

        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = "Searching city..."

        lifecycleScope.launch {

            try {

                val result = RetrofitClient.geocodingApi
                    .searchCity(cityName)

                val city = result.results?.firstOrNull()

                if (city == null) {
                    binding.tvStatus.text = "City not found"
                    return@launch
                }

                val location = if (!city.country.isNullOrBlank()) {
                    "${city.name}, ${city.country}"
                } else {
                    city.name
                }

                loadWeather(
                    city.latitude,
                    city.longitude,
                    location
                )

            } catch (e: Exception) {

                binding.tvStatus.text = "City search failed"

                Toast.makeText(
                    this@MainActivity,
                    "Search failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Load Weather
    private fun loadWeather(
        latitude: Double,
        longitude: Double,
        location: String
    ) {

        binding.tvStatus.visibility = View.VISIBLE
        binding.tvStatus.text = "Loading weather..."

        lifecycleScope.launch {

            try {

                val response = RetrofitClient.weatherApi.getWeather(
                    latitude = latitude,
                    longitude = longitude
                )

                binding.tvLocation.text = location

                showWeather(response)

                loadAirQuality(latitude, longitude)

                binding.tvStatus.visibility = View.GONE

            } catch (e: Exception) {

                binding.tvStatus.text =
                    "Unable to load weather. Check internet."

                binding.tvStatus.visibility = View.VISIBLE

                Toast.makeText(
                    this@MainActivity,
                    "Weather loading failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // AIR QUALITY API
    private fun loadAirQuality(
        latitude: Double,
        longitude: Double
    ) {

        binding.tvUsAqi.text = "--"
        binding.tvEuropeanAqi.text = "--"

        binding.tvUsAqiLevel.text = "Loading..."
        binding.tvEuropeanAqiLevel.text = "Loading..."

        setMetric(R.id.cardPm25, "PM2.5", "Loading...")
        setMetric(R.id.cardPm10, "PM10", "Loading...")
        setMetric(R.id.cardCo, "CO", "Loading...")
        setMetric(R.id.cardNo2, "NO₂", "Loading...")
        setMetric(R.id.cardSo2, "SO₂", "Loading...")
        setMetric(R.id.cardO3, "O₃", "Loading...")

        lifecycleScope.launch {

            try {

                val response = RetrofitClient.airQualityApi.getAirQuality(
                    latitude = latitude,
                    longitude = longitude
                )

                showAirQuality(response)

            } catch (e: Exception) {

                binding.tvUsAqi.text = "--"
                binding.tvEuropeanAqi.text = "--"

                binding.tvUsAqiLevel.text = "Unavailable"
                binding.tvEuropeanAqiLevel.text = "Unavailable"

                setMetric(R.id.cardPm25, "PM2.5", "--")
                setMetric(R.id.cardPm10, "PM10", "--")
                setMetric(R.id.cardCo, "CO", "--")
                setMetric(R.id.cardNo2, "NO₂", "--")
                setMetric(R.id.cardSo2, "SO₂", "--")
                setMetric(R.id.cardO3, "O₃", "--")
            }
        }
    }

    private fun showAirQuality(data: AirQualityResponse) {

        val current = data.current

        binding.tvUsAqi.text =
            current.usAqi?.toString() ?: "--"

        binding.tvUsAqiLevel.text =
            getUsAqiLevel(current.usAqi)

        binding.tvEuropeanAqi.text =
            current.europeanAqi?.toString() ?: "--"

        binding.tvEuropeanAqiLevel.text =
            getEuropeanAqiLevel(current.europeanAqi)

        setMetric(
            R.id.cardPm25,
            "PM2.5",
            formatPollutant(current.pm25)
        )

        setMetric(
            R.id.cardPm10,
            "PM10",
            formatPollutant(current.pm10)
        )

        setMetric(
            R.id.cardCo,
            "CO",
            formatPollutant(current.carbonMonoxide)
        )

        setMetric(
            R.id.cardNo2,
            "NO₂",
            formatPollutant(current.nitrogenDioxide)
        )

        setMetric(
            R.id.cardSo2,
            "SO₂",
            formatPollutant(current.sulphurDioxide)
        )

        setMetric(
            R.id.cardO3,
            "O₃",
            formatPollutant(current.ozone)
        )
    }

    private fun getUsAqiLevel(aqi: Int?): String {

        return when (aqi) {
            null -> "Unavailable"
            in 0..50 -> "Good"
            in 51..100 -> "Moderate"
            in 101..150 -> "Unhealthy (Sensitive)"
            in 151..200 -> "Unhealthy"
            in 201..300 -> "Very Unhealthy"
            else -> "Hazardous"
        }
    }

    private fun getEuropeanAqiLevel(aqi: Int?): String {

        return when (aqi) {
            null -> "Unavailable"
            in 0..20 -> "Good"
            in 21..40 -> "Fair"
            in 41..60 -> "Moderate"
            in 61..80 -> "Poor"
            in 81..100 -> "Very Poor"
            else -> "Extremely Poor"
        }
    }

    private fun formatPollutant(value: Double?): String {

        return value?.let {
            String.format(
                Locale.US,
                "%.1f µg/m³",
                it
            )
        } ?: "--"
    }

    // WEATHER FUNCTIONS

    private fun convertTemperature(celsius: Double): Double {

        return if (isCelsius) {
            celsius
        } else {
            (celsius * 9.0 / 5.0) + 32.0
        }
    }

    // Show Weather
    private fun showWeather(data: WeatherResponse) {

        latestWeather = data

        val current = data.current

        updateWeatherBackground(
            current.weatherCode,
            current.isDay
        )

        updateWeatherAnimation(
            current.weatherCode,
            current.isDay
        )

        val temperature = convertTemperature(current.temperature)
        val feelsLike = convertTemperature(current.feelsLike)

        val unit = if (isCelsius) "C" else "F"

        binding.tvTemp.text =
            "${temperature.toInt()}°$unit"

        binding.tvFeels.text =
            "Feels like ${feelsLike.toInt()}°$unit"

        binding.tvUnitToggle.text =
            if (isCelsius) "°C" else "°F"

        binding.tvCondition.text =
            getWeatherCondition(current.weatherCode)

        binding.tvIcon.text =
            getWeatherIcon(current.weatherCode, current.isDay)

        setMetric(
            R.id.cardHumidity,
            "Humidity",
            "${current.humidity}%"
        )

        setMetric(
            R.id.cardWind,
            "Wind Speed",
            "${current.windSpeed} km/h"
        )

        setMetric(
            R.id.cardRain,
            "Precipitation",
            "${current.precipitation} mm"
        )

        val uv = data.daily.uvIndexMax.firstOrNull() ?: 0.0

        setMetric(
            R.id.cardUv,
            "UV Index",
            String.format(Locale.US, "%.1f", uv)
        )

        showSunriseSunset(data)
        showHourlyForecast(data)
        showDailyForecast(data)
    }

    // LOTTIE WEATHER ANIMATION
    private fun updateWeatherAnimation(
        weatherCode: Int,
        isDay: Int
    ) {

        val animationFile = when {

            weatherCode in 95..99 ->
                "thunderstorm.json"

            weatherCode in 71..77 ||
                    weatherCode in 85..86 ->
                "snow.json"

            weatherCode in 51..67 ||
                    weatherCode in 80..82 ->
                "rainy.json"

            weatherCode == 0 && isDay == 1 ->
                "sunny.json"

            weatherCode == 1 || weatherCode == 2 ->
                "partly_cloudy.json"

            else ->
                "cloudy.json"
        }

        if (currentAnimationFile == animationFile) {
            return
        }

        currentAnimationFile = animationFile

        binding.lottieWeather.cancelAnimation()
        binding.lottieWeather.setAnimation(animationFile)

        binding.lottieWeather.repeatCount =
            LottieDrawable.INFINITE

        binding.lottieWeather.repeatMode =
            LottieDrawable.RESTART

        binding.lottieWeather.playAnimation()
    }

    // Sunrise & Sunset
    private fun showSunriseSunset(data: WeatherResponse) {

        val sunrise = data.daily.sunrise.firstOrNull()
        val sunset = data.daily.sunset.firstOrNull()

        binding.tvSunrise.text = formatSunTime(sunrise)
        binding.tvSunset.text = formatSunTime(sunset)
    }

    private fun formatSunTime(value: String?): String {

        if (value.isNullOrBlank()) return "--:--"

        return try {

            val timePart = value.substringAfter("T")
            val parts = timePart.split(":")

            val hour = parts[0].toInt()
            val minute = parts[1].take(2).toInt()

            val hour12 = when (hour % 12) {
                0 -> 12
                else -> hour % 12
            }

            val period = if (hour < 12) "AM" else "PM"

            String.format(
                Locale.US,
                "%02d:%02d %s",
                hour12,
                minute,
                period
            )

        } catch (e: Exception) {
            "--:--"
        }
    }

    // Dynamic Weather Background
    private fun updateWeatherBackground(
        weatherCode: Int,
        isDay: Int
    ) {

        val backgroundRes = when {

            weatherCode in 51..67 ||
                    weatherCode in 80..82 ||
                    weatherCode in 95..99 -> {
                R.drawable.bg_rainy
            }

            weatherCode in setOf(
                1, 2, 3,
                45, 48,
                71, 73, 75, 77,
                85, 86
            ) -> {
                R.drawable.bg_cloudy
            }

            isDay == 0 -> {
                R.drawable.bg_night
            }

            else -> {
                R.drawable.bg_sunny
            }
        }

        binding.mainScrollView.setBackgroundResource(backgroundRes)
    }

    // Hourly Forecast
    private fun showHourlyForecast(data: WeatherResponse) {

        val hourly = data.hourly

        val count = minOf(
            hourly.time.size,
            hourly.temperature.size,
            hourly.weatherCode.size,
            24
        )

        val items = (0 until count).map { index ->

            HourlyItem(
                time = hourly.time[index],
                temperature = hourly.temperature[index],
                weatherCode = hourly.weatherCode[index]
            )
        }

        binding.rvHourly.adapter =
            HourlyAdapter(items, isCelsius)
    }

    // Daily Forecast
    private fun showDailyForecast(data: WeatherResponse) {

        val daily = data.daily

        val count = minOf(
            daily.time.size,
            daily.weatherCode.size,
            daily.temperatureMin.size,
            daily.temperatureMax.size
        )

        val items = (0 until count).map { index ->

            DailyItem(
                date = daily.time[index],
                weatherCode = daily.weatherCode[index],
                minTemp = daily.temperatureMin[index],
                maxTemp = daily.temperatureMax[index]
            )
        }

        binding.rvDaily.adapter =
            DailyAdapter(items, isCelsius)
    }

    // Set Weather Metrics
    private fun setMetric(
        cardId: Int,
        label: String,
        value: String
    ) {

        val card = findViewById<ViewGroup>(cardId)

        card.findViewById<TextView>(
            R.id.tvMetricLabel
        ).text = label

        card.findViewById<TextView>(
            R.id.tvMetricValue
        ).text = value
    }

    // Weather Condition
    private fun getWeatherCondition(code: Int): String {

        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rainy"
            66, 67 -> "Freezing Rain"
            71, 73, 75, 77 -> "Snowfall"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }

    // Weather Icons
    private fun getWeatherIcon(
        code: Int,
        isDay: Int
    ): String {

        return when (code) {
            0 -> if (isDay == 1) "☀️" else "🌙"
            1, 2 -> "🌤️"
            3 -> "☁️"
            45, 48 -> "🌫️"
            51, 53, 55 -> "🌦️"
            61, 63, 65 -> "🌧️"
            66, 67 -> "🌧️"
            71, 73, 75, 77 -> "❄️"
            80, 81, 82 -> "🌦️"
            85, 86 -> "🌨️"
            95, 96, 99 -> "⛈️"
            else -> "🌡️"
        }
    }

    override fun onDestroy() {
        binding.lottieWeather.cancelAnimation()
        super.onDestroy()
    }
}
