plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.suntrackir"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.suntrackir"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.20"
    }
}

dependencies {
    // تاریخ قمری
    implementation(libs.ummalqura)

    // تاریخ میلادی/محاسبات زمان
    implementation(libs.threetenabp)

    // مکان‌یابی گوگل
    implementation(libs.play.services.location)

    // متریال و Activity برای Compose
    implementation(libs.material)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.lottie.compose)

    // Room
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    // Coroutines برای ViewModel
    implementation(libs.kotlinx.coroutines)

    // تست
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}