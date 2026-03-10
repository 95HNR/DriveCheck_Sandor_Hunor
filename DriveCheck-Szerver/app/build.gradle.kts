plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sofornyilvantarto.uj"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.sofornyilvantarto.uj"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.code.gson:gson:2.10.1")
    // Room adatbázis (JSON helyett)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // RecyclerView (a lista megjelenítéséhez)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Material Design (szebb gombok és beviteli mezők)
    implementation("com.google.android.material:material:1.12.0")
}