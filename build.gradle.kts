import korlibs.korge.gradle.coroutinesVersion
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
        js("worker", IR) {
            binaries.executable()
            browser {
                webpackTask {
                    outputFileName = "worker.js"
                }
                @Suppress("OPT_IN_USAGE")
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
        val commonWorker by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonMain by getting {
            dependencies {
                implementation("co.touchlab:stately-concurrent-collections:2.0.0-rc1")
            }
        }
        val commonBenchmark by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.7")
            }
        }
        val workerMain by getting {
            dependsOn(commonWorker)
        }
        val jsMain by getting {
            dependsOn(commonWorker)
            resources.srcDirs("./build/worker")
        }
        val jvmMain by getting
        val jvmBenchmark by getting {
            dependsOn(commonBenchmark)
            dependsOn(jvmMain)
        }
    }
}
tasks["jsProcessResources"]
    .dependsOn("workerBrowserDistribution")

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
