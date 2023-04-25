package io.github.smaugfm.game2048

suspend fun main() {
    val injector = createInjector()
    startKorge(injector)
}

