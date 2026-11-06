plugins {
    alias(libs.plugins.android.application)
}

// A Runtime Resource Overlay: an APK with resources and no code. Same platform
// signing as the apps so the "signature" overlayable policy is satisfied too.
val platformKeystore = rootProject.file("tools/keys/platform.p12")

android {
    namespace = "dev.headhunt.overlay.systemui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.headhunt.overlay.systemui"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        if (platformKeystore.exists()) {
            create("platform") {
                storeFile = platformKeystore
                storeType = "PKCS12"
                storePassword = "android"
                keyAlias = "platform"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        debug { if (platformKeystore.exists()) signingConfig = signingConfigs.getByName("platform") }
        release {
            isMinifyEnabled = false
            if (platformKeystore.exists()) signingConfig = signingConfigs.getByName("platform")
        }
    }

    // Overlays carry only resources. Keep AAPT from stripping "unused" ones.
    androidResources { ignoreAssetsPattern = "" }
    buildFeatures {
        buildConfig = false
        resValues = false
    }
    lint { lintConfig = file("lint.xml") }
}
