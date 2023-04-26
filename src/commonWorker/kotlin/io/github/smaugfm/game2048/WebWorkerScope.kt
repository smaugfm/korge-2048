package io.github.smaugfm.game2048

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.url.URLSearchParams

class WebWorkerScope(
    private val self: DedicatedWorkerGlobalScope,
    scope: CoroutineScope
) : CoroutineScope by scope {
    val workerId = URLSearchParams(self.location.search).get("id")
        ?: "Unknown worker"

    fun onMessage(block: suspend (String) -> String) {
        self.onmessage = { messageEvent ->
            launch {
                val result = block(messageEvent.data.toString())
                self.postMessage(result)
            }
        }
    }

    companion object {
        fun CoroutineScope.webWorker(block: WebWorkerScope.() -> Unit) {
            val isWorkerGlobalScope =
                js("typeof(WorkerGlobalScope) !== \"undefined\"") as? Boolean
                    ?: throw IllegalStateException("Boolean cast went wrong")
            if (!isWorkerGlobalScope) return

            val self = js("self") as? DedicatedWorkerGlobalScope
                ?: throw IllegalStateException("DedicatedWorkerGlobalScope cast went wrong")

            WebWorkerScope(self, this).block()
        }
    }
}
