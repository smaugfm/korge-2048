@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

fun KotlinJsTargetDsl.configureJsOrWasm() {
    binaries.executable()
    val targetName = this@configureJsOrWasm.name
    browser {
        webpackTask {
            mainOutputFileName = "$targetName.js"
        }
        @OptIn(ExperimentalDistributionDsl::class)
        distribution {
            distributionName = targetName
        }
    }
}

val distribution: NamedDomainObjectProvider<Configuration> by configurations.registering {
    isCanBeConsumed = true
    isCanBeResolved = false
}

kotlin {
    jvm()
    js { configureJsOrWasm() }
    wasmJs { configureJsOrWasm() }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
        commonMain {
            dependencies {
                implementation(projects.common)
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
}

//https://stackoverflow.com/questions/63858392/gradle-copy-submodules-output-into-other-submodule-resources/76489643
artifacts {
    add(distribution.name, tasks.named("jsBrowserDistribution"))
    add(distribution.name, tasks.named("wasmJsBrowserDistribution"))
}

fun KotlinSourceSet.kotlinxDeps() {
    dependencies {
        implementation(libs.kotlinx.serialization)
        implementation(libs.kotlinx.coroutines)
    }
}
