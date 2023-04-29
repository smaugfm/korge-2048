package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import org.w3c.dom.DedicatedWorkerGlobalScope

fun main() {
    val expectimax =
        Expectimax(HashMapTranspositionTable())

    println("Web-worker (wasm) started")

    val self = js("self") as DedicatedWorkerGlobalScope
    self.onmessage = { messageEvent ->
        val requestStr = messageEvent.data.toString()

        val request = ScoreRequest.deserialize(requestStr)!!
        expectimax.transpositionTable.clear()

        val scoreResult = expectimax.score(request)
        self.postMessage(
            scoreResult
                ?.serialize()
                ?.asDynamic()
        )
        null
    }
}
