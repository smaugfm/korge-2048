package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.WebWorkerScope.Companion.webWorker
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.expectimax.FindBestMove
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import korlibs.io.serialization.json.Json

@Suppress("UNCHECKED_CAST")
fun main() =
    webWorker {
        val expectimax =
            Expectimax(Direction.valueOf(workerId), HashMapTranspositionTable())

        println("Web-worker id=$workerId started")
        onMessage {
            val requestMap = Json.parse(it) as Map<String, String>

            val request = FindBestMove.Companion.ScoreRequest.fromMap(requestMap)
            expectimax.transpositionTable.clear()

            val result = expectimax.score(request.board, request.depthLimit)
            Json.stringify(result?.toMap())
        }
    }

