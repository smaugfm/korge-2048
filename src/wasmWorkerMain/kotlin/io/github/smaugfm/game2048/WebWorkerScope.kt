package io.github.smaugfm.game2048

import org.w3c.dom.DedicatedWorkerGlobalScope

class WebWorkerScope(
    private val self: DedicatedWorkerGlobalScope,
) {
    fun onMessage(block: (String) -> String) {
        self.onmessage = { messageEvent ->
            val result = block(messageEvent.data.toString())
            self.postMessage(result)
            null
        }
    }

    companion object {
        fun webWorker(block: WebWorkerScope.() -> Unit) {
            val self = js("self") as DedicatedWorkerGlobalScope
            WebWorkerScope(self).block()
        }
    }
}
