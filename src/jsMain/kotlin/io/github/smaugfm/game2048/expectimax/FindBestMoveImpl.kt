package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.send
import korlibs.io.serialization.json.Json
import org.w3c.dom.Worker

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove() {
    private val workers =
        directions.map {
            Worker("./worker.js?id=$it")
        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun scoreAllDirections(
        req: ScoreRequest
    ): List<ExpectimaxResult?> =
        workers.map { worker ->
            val reqMapStr = Json.stringify(req.toMap())
            val evt = worker.send(reqMapStr)
            if (evt.data == null || evt.data.toString() == "null")
                return@map null

            val map = Json.parse(evt.data.toString()) as Map<String, Any>
            ExpectimaxResult.fromMap(map)
        }
}
