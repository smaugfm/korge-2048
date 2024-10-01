plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()
    js { browser() }
}
