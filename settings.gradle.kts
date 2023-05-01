rootProject.name = "game2048"

pluginManagement {
    val korgeVersion: String by settings
    val kotlinxBenchmarkVersion: String by settings
    val allOpenVersion: String by settings

    plugins {
        id("com.soywiz.korge") version korgeVersion
        id("org.jetbrains.kotlin.plugin.allopen") version allOpenVersion
        id("org.jetbrains.kotlinx.benchmark") version kotlinxBenchmarkVersion
    }

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
