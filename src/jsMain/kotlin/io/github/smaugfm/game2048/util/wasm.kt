package io.github.smaugfm.game2048.util

import kotlinx.coroutines.delay

suspend fun checkWasmSupported(): Boolean {
    while (true) {
        try {
            return js("hasWasmSupport").unsafeCast<Boolean>()
        } catch (e: Throwable) {
            delay(1000)
        }
    }
}
