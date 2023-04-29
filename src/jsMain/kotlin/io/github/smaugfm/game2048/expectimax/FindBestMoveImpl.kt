package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.util.send
import org.w3c.dom.Worker

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove() {
    private val workers =
        directions.map {
            Worker("./wasm-worker.js")
        }

    override suspend fun scoreAllDirections(
        requests: List<ScoreRequest>,
    ): Pair<List<Companion.ScoreResult>, ExpectimaxDiagnostics?> =
        workers.zip(requests).mapNotNull { (worker, req) ->
            val evt = worker.send(req.serialize())
            ExpectimaxResult.deserialize(evt.data.toString())
        }.let(::transformResults)

    private fun transformResults(results: List<ExpectimaxResult>) =
        Pair(
            results.map { Companion.ScoreResult(it.score, it.direction) },
            results.map { it.diagnostics }
                .reduce(ExpectimaxDiagnostics::combineShared)
        )
}
