plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.cs3repo.anime"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cs3repo.anime"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val cloudstreamVersion = "f3293d5b66"
    compileOnly("com.github.recloudstream:cloudstream:$cloudstreamVersion")
}
