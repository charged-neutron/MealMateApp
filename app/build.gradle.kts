plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.platepal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.platepal"
        minSdk = 24
        targetSdk = 36
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- FIREBASE DEPENDENCIES ---

    // 1. Import the Firebase BoM (Bill of Materials)
    // This will manage the versions of all Firebase libraries.
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // 2. Dependency for Firebase Authentication
    // Use "ktx" for Kotlin extensions
    implementation("com.google.firebase:firebase-auth-ktx")

    // 3. Dependency for Firebase Realtime Database
    // Use "ktx" for Kotlin extensions
    implementation("com.google.firebase:firebase-database-ktx")

    // 4. Dependency for Firebase Cloud Messaging (for Push Notifications)
    // Use "ktx" for Kotlin extensions
    implementation("com.google.firebase:firebase-messaging-ktx")
}