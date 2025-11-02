pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ CORRECTED: The URL for JitPack is now correct.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "EnergyMeterApp"
include(":app")
