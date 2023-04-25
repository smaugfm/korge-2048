package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.WebWorkerScope.Companion.webWorker
import kotlinx.coroutines.coroutineScope

suspend fun main() =
    coroutineScope {
        webWorker {
            onMessage {
                val reply = "Hello from $workerId"
                println("Worker #$workerId Received message $it. Sending reply $reply")
                reply
            }
        }
    }

