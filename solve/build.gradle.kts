import korlibs.korge.gradle.BuildVersions
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    js { browser() }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        commonMain {
            dependencies {
                implementation(project(":common"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmMain {
            kotlinxDeps()
        }
        jsMain {
            kotlinxDeps()
        }
    }
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

fun KotlinSourceSet.kotlinxDeps() {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${BuildVersions.KOTLIN_SERIALIZATION}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${BuildVersions.COROUTINES}")
    }
}
