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
//  targetJs()
//  targetWasmJs()
  serializationJson()
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
  sourceSets {
    val commonMain by getting
    val commonKorge by creating {
      dependsOn(commonMain)
      dependencies {
        implementation("com.soywiz.korge:korge-core:${BuildVersions.KORGE}")
        implementation("com.soywiz.korge:korge:${BuildVersions.KORGE}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${BuildVersions.KOTLIN_SERIALIZATION}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${BuildVersions.COROUTINES}")
      }
    }
    val jvmMain by getting {
      dependsOn(commonKorge)
    }
  }
}

