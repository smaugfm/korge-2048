enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "game2048"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

include("gui")
include("common")
include("solve")
include("benchmark")
