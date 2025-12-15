plugins {
    alias(libs.plugins.android.application)

    // Necesario para integrar las dependencias de Google Services (Firebase)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.formularioapp"
    compileSdk {
        // Asegúrate de que esta versión coincida con tu entorno
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.formularioapp"
        minSdk = 23
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
    // Dependencias de Android existentes
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.airbnb.android:lottie:6.7.1")

    // 1. GLIDE (Para carga de imágenes)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // 2. CONFIGURACIÓN DE FIREBASE Y GOOGLE SIGN-IN
    // Importar la Firebase BoM (Bill of Materials) para gestionar versiones (Usamos la versión 34.0.0)
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))

    // Modulos de Firebase (SIN VERSIONES, gestionadas por la BoM):
    implementation("com.google.firebase:firebase-auth")       // Autenticación
    implementation("com.google.firebase:firebase-firestore")  // Base de datos
    implementation("com.google.firebase:firebase-storage")    // Almacenamiento
    implementation("com.google.android.gms:play-services-maps:18.2.0")
// Si usas Firebase UI para Firestore, intenta excluir el módulo de Annotations
    // o cualquier módulo común que Google Play Services pueda duplicar.

    // Google Location Services (necesario para obtener la ubicación actual FusedLocationClient)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Firebase UI
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // Google Sign-In (SÍ NECESITA SU VERSIÓN EXPLICITA o la que mejor funcione con tu entorno)
    // Mantendremos la versión que tenías para evitar problemas de compatibilidad inmediata.

    // Opcionalmente, puedes intentar usar la versión de la BoM si es compatible:
    // implementation 'com.google.android.gms:play-services-auth'

    // Dependencias de testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}