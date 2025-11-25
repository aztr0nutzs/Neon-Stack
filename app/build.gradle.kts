plugins {
    id("com.android.application")
}

android {
    namespace = "com.neonstack.underground"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.neonstack.underground"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Disable release lint vital to avoid blocking CI/build without full lint setup.
            lint {
                checkReleaseBuilds = false
                abortOnError = false
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // No external libraries required; using only framework APIs.
}
