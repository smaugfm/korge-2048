package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.search.ExpectimaxSearch
import io.github.smaugfm.game2048.search.SearchRequest
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import org.w3c.dom.DedicatedWorkerGlobalScope

fun main() {
    val table = HashMapTranspositionTable()

    println("Web-worker (wasm) started")

    val self = js("self") as DedicatedWorkerGlobalScope
    self.onmessage = { messageEvent ->
        val requestStr = messageEvent.data.toString()

        val request = SearchRequest.deserialize(requestStr)!!
        table.clear()

        val scoreResult = ExpectimaxSearch(table).score(request)
        self.postMessage(
            scoreResult
                ?.serialize()
                ?.asDynamic()
        )
        null
    }
}
