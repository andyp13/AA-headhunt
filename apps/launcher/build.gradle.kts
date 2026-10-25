import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Platform signing. tools/keys/make-platform-keystore.sh produces platform.p12
// from the AOSP test keys. When it exists every build type is signed with it so
// the emulator grants the signature-level car permissions. When it is absent
// (CI, a fresh clone) the build falls back to the normal debug key and still
// compiles; it just won't be able to read privileged vehicle properties.
val platformKeystore = rootProject.file("tools/keys/platform.p12")

android {
    namespace = "dev.headhunt.launcher"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.headhunt.launcher"
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
        debug {
            if (platformKeystore.exists()) signingConfig = signingConfigs.getByName("platform")
        }
        release {
            isMinifyEnabled = false
            if (platformKeystore.exists()) signingConfig = signingConfigs.getByName("platform")
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":libs:design-system"))
    implementation(project(":libs:car-kit"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
