plugins {
  alias(libs.plugins.korge) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
}
allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
    google()
  }
}
