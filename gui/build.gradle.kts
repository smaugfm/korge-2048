import korlibs.korge.gradle.BuildVersions
import korlibs.korge.gradle.korge

plugins {
    alias(libs.plugins.korge)
}

korge {
    id = "io.github.smaugfm.game2048"
    name = "game2048"
    skipDeps = true

    jvmMainClassName = "io.github.smaugfm.game2048.MainKt"
    entrypoint("main", "io.github.smaugfm.game2048.MainKt")

    targetJvm()
    targetJs()
//  targetWasmJs()
    serializationJson()
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(project(":solve"))
                implementation("com.soywiz.korge:korge-core:${BuildVersions.KORGE}")
                implementation("com.soywiz.korge:korge:${BuildVersions.KORGE}")
            }
        }
    }
}

