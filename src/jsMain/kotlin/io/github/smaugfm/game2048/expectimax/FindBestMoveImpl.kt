package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.send
import org.w3c.dom.Worker

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove() {
    private val workers =
        directions.map {
            Worker("./worker.js?id=$it")
        }

    override suspend fun scoreAllDirections(
        req: ScoreRequest
    ): List<Expectimax.ExpectimaxResult?> =
        workers.map {
            val evt = it.send(JSON.stringify(req))
            JSON.parse(evt.data.toString())
        }
}
