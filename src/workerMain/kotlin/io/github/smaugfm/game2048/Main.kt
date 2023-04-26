package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.WebWorkerScope.Companion.webWorker
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import kotlinx.coroutines.coroutineScope

suspend fun main() =
    coroutineScope {
        webWorker {
            val expectimax =
                Expectimax(Direction.valueOf(workerId), HashMapTranspositionTable())

            println("Web-worker id=$workerId started")
            onMessage {
                val request = JSON.parse<ScoreRequest>(it)
                expectimax.transpositionTable.clear()
                println("Web-worker id=$workerId received message $request")
                val result = expectimax.score(request.board, request.depthLimit)
                JSON.stringify(result)
            }
        }
    }

