@file:Suppress("OPT_IN_USAGE")

import korlibs.korge.gradle.BuildVersions
import korlibs.korge.gradle.coroutinesVersion
import korlibs.korge.gradle.kdsVersion
import korlibs.korge.gradle.klockVersion
import korlibs.korge.gradle.kmemVersion
import korlibs.korge.gradle.korauVersion
import korlibs.korge.gradle.korgwVersion
import korlibs.korge.gradle.korimVersion
import korlibs.korge.gradle.korioVersion
import korlibs.korge.gradle.kormaVersion
import korlibs.korge.gradle.kryptoVersion
import kotlinx.benchmark.gradle.BenchmarksExtension
import kotlinx.benchmark.gradle.KotlinJvmBenchmarkTarget

plugins {
    id("com.soywiz.korge")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("org.jetbrains.kotlinx.benchmark")
}

val korgeVersion: String by project

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

val benchmarksExtension: BenchmarksExtension = the<BenchmarksExtension>()

korge {
    id = "io.github.smaugfm.game2048"
    name = "game2048"
    jvmMainClassName = "io.github.smaugfm.game2048.MainKt"

    targetJvm()
    targetJs()
}

afterEvaluate {
    project.configurations
        .filter { it.name.startsWith("commonMain") }
        .forEach { conf ->
            conf.dependencies
                .removeIf {
                    it.group?.startsWith("com.soywiz") == true ||
                        it.group?.startsWith("korlibs") == true ||
                        (it.group == "org.jetbrains.kotlinx" && it.name == "kotlinx-coroutines-core") ||
                        (it.group == "org.jetbrains.kotlinx" && it.name == "kotlinx-serialization-json")
                }
        }
}

kotlin {
    targets {
        jvm {
            val benchmarkCompilation = compilations
                .create("benchmark")

            benchmarksExtension.targets.add(
                KotlinJvmBenchmarkTarget(
                    extension = benchmarksExtension,
                    name = "jvm",
                    compilation = benchmarkCompilation
                ).apply {
                    val jmhVersion: String by project
                    this.jmhVersion = jmhVersion
                }
            )
        }
        js("jsWorker", IR) {
            binaries.executable()
            browser {
                webpackTask {
                    outputFileName = "worker.js"
                }
                distribution {
                    name = "worker"
                }
            }
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
        }
        val commonMain by getting
        val commonKorge by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${BuildVersions.KOTLIN_SERIALIZATION}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.soywiz.korlibs.klock:klock:${klockVersion}")
                implementation("com.soywiz.korlibs.kmem:kmem:${kmemVersion}")
                implementation("com.soywiz.korlibs.kds:kds:${kdsVersion}")
                implementation("com.soywiz.korlibs.krypto:krypto:${kryptoVersion}")
                implementation("com.soywiz.korlibs.korge2:korge:${korgeVersion}")
                implementation("com.soywiz.korlibs.korma:korma:${kormaVersion}")
                implementation("com.soywiz.korlibs.korio:korio:${korioVersion}")
                implementation("com.soywiz.korlibs.korim:korim:${korimVersion}")
                implementation("com.soywiz.korlibs.korau:korau:${korauVersion}")
                implementation("com.soywiz.korlibs.korgw:korgw:${korgwVersion}")
            }
        }
        val commonBenchmark by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.7")
            }
        }
        val jsWorkerMain by getting
        val jsMain by getting {
            dependsOn(commonKorge)
            resources.srcDirs("./build/worker")
        }
        val jvmMain by getting {
            dependsOn(commonKorge)
        }
        val jvmBenchmark by getting {
            dependsOn(commonBenchmark)
            dependsOn(jvmMain)
        }
    }
}

tasks {
    getByName("jsProcessResources") {
        dependsOn("jsWorkerBrowserDistribution")
    }
}

benchmark {
    configurations {
        register("findBestMove") {
            include(".*FindBestMoveBenchmark.*")
        }
        register("moveBoard") {
            include(".*MoveBoardBenchmark.*")
        }
    }
}
