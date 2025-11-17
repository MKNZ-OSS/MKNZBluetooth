plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.mknz.bluetooth"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mknz.bluetooth"
        minSdk = 35
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"
    }

    signingConfigs {
        getByName("debug") {
            val MKNZ_STORE_FILE: String by project
            val MKNZ_STORE_PASSWORD: String by project
            val MKNZ_KEY_ALIAS: String by project
            val MKNZ_KEY_PASSWORD: String by project
            storeFile = file(MKNZ_STORE_FILE)
            storePassword = MKNZ_STORE_PASSWORD
            keyAlias = MKNZ_KEY_ALIAS
            keyPassword = MKNZ_KEY_PASSWORD
        }
        create("release") {
            val MKNZ_STORE_FILE: String by project
            val MKNZ_STORE_PASSWORD: String by project
            val MKNZ_KEY_ALIAS: String by project
            val MKNZ_KEY_PASSWORD: String by project
            storeFile = file(MKNZ_STORE_FILE)
            storePassword = MKNZ_STORE_PASSWORD
            keyAlias = MKNZ_KEY_ALIAS
            keyPassword = MKNZ_KEY_PASSWORD
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.androidx.constraintlayout)
}
