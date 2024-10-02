import korlibs.korge.gradle.BuildVersions
import korlibs.korge.gradle.korge
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

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
    targetWasmJs()
    serializationJson()
}

val generatedOutput: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

//https://stackoverflow.com/questions/63858392/gradle-copy-submodules-output-into-other-submodule-resources/76489643
val copyGeneratedOutput: TaskProvider<Copy> by tasks.registering(Copy::class) {
    from(generatedOutput)
    into(project.layout.buildDirectory.dir("generatedOutput"))
    include { elem ->
        listOf(".js", ".js.map", ".wasm")
            .any(elem.name::endsWith)
    }
}

afterEvaluate {
    tasks {
        getByName("jsCreateIndexHtml").dependsOn(copyGeneratedOutput)
    }
}

dependencies {
    generatedOutput(projects.solve) {
        targetConfiguration = "distribution"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.common)
                implementation(projects.solve)
                implementation(libs.korge)
                implementation(libs.korge.core)
            }
        }
        jsMain {
            resources.srcDir(copyGeneratedOutput)
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

