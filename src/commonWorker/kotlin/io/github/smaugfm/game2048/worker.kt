package io.github.smaugfm.game2048

import org.w3c.dom.Worker
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun Worker.send(data: String) = suspendCoroutine { continuation ->
    this.onmessage = { messageEvent ->
        continuation.resume(messageEvent)
    }
    this.onerror =
        { event -> continuation.resumeWithException(RuntimeException(event.type)) }

    this.postMessage(data)
}
