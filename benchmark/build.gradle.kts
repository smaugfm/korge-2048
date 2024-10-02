@file:OptIn(ExperimentalWasmDsl::class)

import kotlinx.benchmark.gradle.BenchmarksExtension
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxBenchmark)
    alias(libs.plugins.allopen)
}

val benchmarksExtension: BenchmarksExtension = the<BenchmarksExtension>()

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common"))
                implementation(project(":solve"))
                implementation(libs.kotlinx.benchmark.runtime)
            }
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("jvm")
    }
    configurations {
        register("findBestMove") {
            warmups = 2
            iterations = 2
            outputTimeUnit = "s"
            include(".*FindBestMoveBenchmark.*")
        }
        register("moveBoard") {
            warmups = 2
            iterations = 2
            outputTimeUnit = "s"
            include(".*MoveBoardBenchmark.*")
        }
    }
}
