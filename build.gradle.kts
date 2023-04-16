import kotlinx.benchmark.gradle.BenchmarksExtension
import kotlinx.benchmark.gradle.KotlinJvmBenchmarkTarget

plugins {
    id("com.soywiz.korge") version "4.0.0-rc"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.7"
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    targets {
        jvm {
            val benchmarkCompilation = compilations
                .create("benchmark")

            val benchmarksSourceSetName: String = benchmarkCompilation.defaultSourceSet.name
            val benchmarksExtension: BenchmarksExtension = the<BenchmarksExtension>()
            val benchmarkTarget = KotlinJvmBenchmarkTarget(
                extension = benchmarksExtension,
                name = benchmarksSourceSetName,
                compilation = benchmarkCompilation
            ).apply {
                jmhVersion = "1.36"
            }
            benchmarksExtension.targets.add(benchmarkTarget)
        }
    }
    sourceSets {
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

korge {
    id = "io.github.smaugfm.game2048"
    name = "game2048"
    jvmMainClassName = "io.github.smaugfm.game2048.MainKt"
//    To enable all targets at once
//    targetAll()

//    To enable targets based on properties/environment variables
//    targetDefault()

    targetJvm()
    targetJs()
//    targetDesktop()
//    targetIos()
//    targetAndroid()
}
