package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.util.checkWasmSupport

var isWasmSupported = false
    private set

suspend fun main() {
    isWasmSupported = checkWasmSupport()

    val injector = createInjector()
    startKorge(injector)
}
