// This file is the module-level build script for the 'app' module.

plugins {
    // 1. Android Application Plugin (Required for the 'android' block)
    alias(libs.plugins.android.application)

    // 2. Kotlin Android Plugin (Required for Kotlin support)
    id("org.jetbrains.kotlin.android")

    // 3. Kotlin Symbol Processing (KSP) Plugin (Required for Room, Hilt, etc.)
    id("com.google.devtools.ksp")

    // 4. (Optional) Kotlin Parcelize for easy data serialization
    id("kotlin-parcelize")

    // 5. (Optional) Hilt/Dagger for Dependency Injection
    // id("com.google.dagger.hilt.android") // Uncomment if using Hilt
}

android {
    namespace = "com.example.energymeterapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.energymeterapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configure Kotlin options
    kotlinOptions {
        jvmTarget = "11" // Match compileOptions target
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
        // Ensuring compatibility with Java 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // ----------------------------------------------------
    // CORE AND UI DEPENDENCIES
    // ----------------------------------------------------

    // Essential AndroidX Core KTX for utility extensions
    implementation("androidx.core:core-ktx:1.12.0")

    // AppCompat and Material
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Constraint Layout for modern UI layouts
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Third-party UI components:
    // 1. SpeedView for gauges (AwesomeSpeedometer)
    implementation("com.github.anastr:speedviewlib:1.6.0")

    // 2. MPAndroidChart for LineChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Activity and Fragment KTX extensions
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // ----------------------------------------------------
    // ARCHITECTURE COMPONENTS (Lifecycle & LiveData/Flow)
    // ----------------------------------------------------

    // ViewModel and LiveData KTX extensions
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Kotlin Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Networking/Preferences (using version catalog)
    implementation(libs.volley)
    implementation(libs.preference)

    // ----------------------------------------------------
    // DATA PERSISTENCE (ROOM Database)
    // ----------------------------------------------------

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // ----------------------------------------------------
    // TESTING DEPENDENCIES
    // ----------------------------------------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.google.code.gson:gson:2.10.1")
}

// NOTE: The following dependencies were redundant and have been removed:
// implementation("com.github.anastr:speedviewlib:1.6.0")
// implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
// implementation("androidx.cardview:cardview:1.0.0")
// implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
