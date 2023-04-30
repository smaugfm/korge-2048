package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.util.checkWasmSupport

suspend fun main() {
    usingWasm = checkWasmSupport()

    val injector = createInjector()
    startKorge(injector)
}
