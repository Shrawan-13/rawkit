plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.rawkit.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rawkit.launcher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("release") {
            // Populated from -P flags by the release workflow, or from
            // ~/.gradle/gradle.properties for local release builds.
            val storePath = providers.gradleProperty("RAWKIT_STORE_FILE").orNull
            if (storePath != null) {
                storeFile = file(storePath)
                storePassword = providers.gradleProperty("RAWKIT_STORE_PASSWORD").orNull
                keyAlias = providers.gradleProperty("RAWKIT_KEY_ALIAS").orNull
                keyPassword = providers.gradleProperty("RAWKIT_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (providers.gradleProperty("RAWKIT_STORE_FILE").isPresent) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.recyclerview)

    testImplementation(libs.junit)
}
