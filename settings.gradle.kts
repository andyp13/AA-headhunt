pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Lets Gradle download the JDK named in gradle/gradle-daemon-jvm.properties
// when the host's Java is unsuitable (too new for Gradle, odd version string).
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "headhunt"

include(":apps:launcher")
include(":libs:design-system")
include(":libs:car-kit")
include(":platform:overlays:car-systemui")
