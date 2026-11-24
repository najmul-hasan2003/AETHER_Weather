package com.example.aetherweather


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aetherweather.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvPrivacyBack.setOnClickListener {
            finish()
        }
    }
}
