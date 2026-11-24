plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.aetherweather"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.aetherweather"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // OkHttp
    implementation(libs.okhttp.logging)

    // Coroutines
    implementation(libs.coroutines.android)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    // RecyclerView
    implementation(libs.recyclerview)
    implementation(libs.playServicesLocation)
    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.7.1")
    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")
}
