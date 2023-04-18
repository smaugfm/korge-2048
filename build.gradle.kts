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

kotlin {
    targets {
        jvm {
            val benchmarkCompilation = compilations
                .create("benchmark")

            val benchmarksSourceSetName: String =
                benchmarkCompilation.defaultSourceSet.name
            val benchmarksExtension: BenchmarksExtension = the<BenchmarksExtension>()
            val benchmarkTarget = KotlinJvmBenchmarkTarget(
                extension = benchmarksExtension,
                name = benchmarksSourceSetName,
                compilation = benchmarkCompilation
            ).apply {
                val jmhVersion: String by project
                this.jmhVersion = jmhVersion
            }
            benchmarksExtension.targets.add(benchmarkTarget)
        }
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.ExperimentalTime")
        }
        val commonMain by getting
        val jvmMain by getting
        val commonBenchmark by creating {
            dependsOn(commonMain)
        }
        val jvmBenchmark by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.7")
            }
            dependsOn(commonBenchmark)
            dependsOn(jvmMain)
        }
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

korge {
    id = "io.github.smaugfm.game2048"
    name = "game2048"
    jvmMainClassName = "io.github.smaugfm.game2048.MainKt"

    targetJvm()
    targetJs()
    targetDesktop()
}
