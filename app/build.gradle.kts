plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
}

android {
    namespace = "org.jraf.android.simplewatchface3"
    compileSdk = 33

    defaultConfig {
        applicationId = "org.jraf.android.simplewatchface3"
        minSdk = 30
        targetSdk = 33
        versionCode = 4
        versionName = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("SIGNING_STORE_PATH") ?: ".")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true

            // proguard-android-optimize.txt seem to break observability from kprefs! Not sure why.
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.wear.watchface)
    implementation(libs.androidx.wear.watchface.complications.rendering)
    implementation(libs.androidx.wear.watchface.editor)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.lifecycle.runtime.ktx)

//    implementation(libs.horologist.compose.layout)

    implementation(libs.androidx.core.ktx)

    implementation(libs.jraf.kprefs)
    implementation(libs.jraf.wearColorPicker)

    implementation(libs.timber)
}

// To release:
// SIGNING_STORE_PATH=path/to/upload.keystore SIGNING_STORE_PASSWORD=password SIGNING_KEY_ALIAS=upload SIGNING_KEY_PASSWORD=password ./gradlew :app:bundleRelease
