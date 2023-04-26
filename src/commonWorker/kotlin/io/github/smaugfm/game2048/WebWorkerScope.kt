package io.github.smaugfm.game2048

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.DedicatedWorkerGlobalScope
import org.w3c.dom.url.URLSearchParams

@OptIn(DelicateCoroutinesApi::class)
class WebWorkerScope(
    private val self: DedicatedWorkerGlobalScope,
) {
    val workerId = URLSearchParams(self.location.search).get("id")
        ?: "Unknown worker"

    fun onMessage(block: suspend (String) -> String) {
        self.onmessage = { messageEvent ->
            GlobalScope.launch {
                val result = block(messageEvent.data.toString())
                self.postMessage(result)
            }
        }
    }

    companion object {
        fun webWorker(block: WebWorkerScope.() -> Unit) {
            val isWorkerGlobalScope =
                js("typeof(WorkerGlobalScope) !== \"undefined\"") as? Boolean
                    ?: throw IllegalStateException("Boolean cast went wrong")
            if (!isWorkerGlobalScope) return

            val self = js("self") as? DedicatedWorkerGlobalScope
                ?: throw IllegalStateException("DedicatedWorkerGlobalScope cast went wrong")

            WebWorkerScope(self).block()
        }
    }
}
