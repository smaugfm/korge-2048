package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.search.ExpectimaxSearch
import io.github.smaugfm.game2048.search.SearchRequest
import io.github.smaugfm.game2048.transposition.Long2LongMapTranspositionTable
import org.w3c.dom.DedicatedWorkerGlobalScope

fun main() {
    val table = Long2LongMapTranspositionTable()
    val self = js("self") as DedicatedWorkerGlobalScope

    self.onmessage = { messageEvent ->
        try {
            val requestStr = messageEvent.data.toString()
            if (requestStr == "ping") {
                self.postMessage("pong")
            } else {
                val request = SearchRequest.deserialize(requestStr)!!
                val scoreResult = ExpectimaxSearch(table).score(request)
                self.postMessage(
                    scoreResult?.serialize()
                )
            }
            null
        } catch (e: Throwable) {
            println("Unhandled exception in web worker (js):")
            console.error(e)
            null
        }
    }

    println("Web worker (js) started")
}
