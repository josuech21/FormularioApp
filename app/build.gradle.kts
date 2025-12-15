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
    //libreria para animacion (en caso de requerir):
    implementation ("com.airbnb.android:lottie:6.7.1")

    // ===================================================
    // 1. GLIDE (Para carga de imágenes)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // ¡¡¡LÍNEA AÑADIDA!!!: Necesaria para el procesamiento de anotaciones de Glide
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // FIREBASE UI (Para el adaptador de RecyclerView de Firestore)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // 2. DEPENDENCIAS DE FIREBASE
    // Importar la Firebase BoM (Bill of Materials) para gestionar versiones
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))

    // Firebase Authentication (para Login y Registro)
    implementation("com.google.firebase:firebase-auth")

    // Cloud Firestore (LA LIBRERÍA DE BASE DE DATOS)
    implementation("com.google.firebase:firebase-firestore")

    // Opcional: Firebase Storage (para subir fotos de productos)
    implementation("com.google.firebase:firebase-storage")

    // Dependencias de testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}