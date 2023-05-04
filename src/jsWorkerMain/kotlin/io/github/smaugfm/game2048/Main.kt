package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.search.ExpectimaxSearch
import io.github.smaugfm.game2048.search.SearchRequest
import io.github.smaugfm.game2048.transposition.Long2LongMapTranspositionTable
import org.w3c.dom.DedicatedWorkerGlobalScope

fun main() {
    println("Web-worker (js) started")
    val table = Long2LongMapTranspositionTable()

    val self = js("self") as DedicatedWorkerGlobalScope
    self.onmessage = { messageEvent ->
        val requestStr = messageEvent.data.toString()

        val request = SearchRequest.deserialize(requestStr)!!

        val scoreResult = ExpectimaxSearch(table).score(request)
        self.postMessage(
            scoreResult
                ?.serialize()
                ?.asDynamic()
        )
        null
    }
}
