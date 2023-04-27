package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.WebWorkerScope.Companion.webWorker
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable

fun main() =
    webWorker {
        val expectimax =
            Expectimax(Direction.valueOf(workerId), HashMapTranspositionTable())

        println("Web-worker id=$workerId started")
        onMessage {
            val request = ScoreRequest.deserialize(it)!!
            expectimax.transpositionTable.clear()

            val result = expectimax.score(request.board, request.depthLimit)
            result?.serialize() ?: "null"
        }
    }

