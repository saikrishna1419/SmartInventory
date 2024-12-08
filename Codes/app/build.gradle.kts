plugins {
    id("com.android.application")
    id("com.google.gms.google-services") // Apply the Google services plugin for Firebase
}

android {
    namespace = "com.example.smartinventory"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartinventory"
        minSdk = 31
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE"
            )
        }
    }
}



dependencies {
    // Android UI and support libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Firebase dependencies (managed by BOM)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage") // Firebase Storage for uploading PDFs

    // PDF handling libraries
    implementation("com.itextpdf:itext7-core:8.0.5")// iText 7 core library
    implementation("com.itextpdf:layout:8.0.5") // Replace with the latest version if needed
    implementation("com.itextpdf:forms:8.0.5") // Replace with the latest version if needed
    implementation("com.itextpdf:barcodes:8.0.5") // Replace with the latest version if needed
    implementation("com.itextpdf:html2pdf:5.0.5") // Replace with the latest version if needed
    implementation("com.itextpdf:pdfa:5.5.9")
    implementation("com.google.firebase:firebase-database:21.0.0")

    // Testing libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Swipe
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //cloud
    implementation("com.google.firebase:firebase-messaging:24.0.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")// Check for the latest version
    //implementation 'com.google.firebase:firebase-messaging:24.5.0' // Check for the latest version
    implementation("com.google.firebase:firebase-messaging-ktx:24.0.2") // Core Firebase dependency
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.14.0") // Google Auth library



    //Storage
    implementation("com.google.firebase:firebase-storage:20.1.0")








}
