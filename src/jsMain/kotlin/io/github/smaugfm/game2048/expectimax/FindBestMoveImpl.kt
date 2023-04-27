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
        req: ScoreRequest,
    ): Pair<List<Companion.ScoreResult>, ExpectimaxDiagnostics?> =
        workers.mapNotNull { worker ->
            computeScoreInWebWorker(req, worker)
        }.let(::transformResults)

    private suspend fun computeScoreInWebWorker(
        req: ScoreRequest,
        worker: Worker,
    ): ExpectimaxResult? {
        val evt = worker.send(req.serialize())
        return ExpectimaxResult.deserialize(evt.data.toString())
    }

    private fun transformResults(results: List<ExpectimaxResult>) =
        Pair(
            results.map { Companion.ScoreResult(it.score, it.direction) },
            results.map { it.diagnostics }
                .reduce(ExpectimaxDiagnostics::combineShared)
        )
}
