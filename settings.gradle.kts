pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

includeBuild("gradlePlugin/pixabay-album-dependency")

include(
    ":coroutine-helper",
    ":client",
    ":album",
    ":app-android"
)

buildCache {
    local {
        isEnabled = false
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

rootProject.name = "pixabay-album"
include(":app")
