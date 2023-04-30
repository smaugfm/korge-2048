package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.util.checkWasmSupported

suspend fun main() {
    if (!checkWasmSupported())
        return

    val injector = createInjector()
    startKorge(injector)
}
