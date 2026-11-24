package com.example.aetherweather

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.aetherweather.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val preferences by lazy {
        getSharedPreferences("AETHER_SETTINGS", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSavedSettings()

        binding.tvBack.setOnClickListener {
            finish()
        }

        // Privacy Policy
        binding.cardPrivacyPolicy.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        // Temperature Unit
        binding.rgTemperature.setOnCheckedChangeListener { _, checkedId ->

            val isCelsius = checkedId == R.id.rbCelsius

            preferences.edit()
                .putBoolean("is_celsius", isCelsius)
                .apply()

            Toast.makeText(
                this,
                if (isCelsius) "Celsius selected" else "Fahrenheit selected",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Theme Mode
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->

            val themeMode = when (checkedId) {
                R.id.rbDark -> "dark"
                R.id.rbLight -> "light"
                else -> "system"
            }

            preferences.edit()
                .putString("theme_mode", themeMode)
                .apply()

            val nightMode = when (themeMode) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        binding.tvAppVersion.text = "Version 1.0"
    }

    private fun loadSavedSettings() {

        val isCelsius = preferences.getBoolean("is_celsius", true)

        if (isCelsius) {
            binding.rbCelsius.isChecked = true
        } else {
            binding.rbFahrenheit.isChecked = true
        }

        when (preferences.getString("theme_mode", "dark")) {
            "dark" -> binding.rbDark.isChecked = true
            "light" -> binding.rbLight.isChecked = true
            else -> binding.rbSystem.isChecked = true
        }
    }
}
