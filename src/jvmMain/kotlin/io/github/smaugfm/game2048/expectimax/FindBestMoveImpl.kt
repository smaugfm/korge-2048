package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreResult
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.*

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove(log) {
    private val table = ConcurrentHashMapTranspositionTable()

    override suspend fun scoreAllDirections(
        req: ScoreRequest
    ): Pair<List<ScoreResult>, ExpectimaxDiagnostics?> {
        table.clear()

        val results = asyncComputeResults(req)

        return transformResults(results)
    }

    private fun transformResults(results: List<ExpectimaxResult>):
        Pair<List<ScoreResult>, ExpectimaxDiagnostics?> {
        val scoreResults =
            results.map { ScoreResult(it.score, it.direction) }
        val diagnostics = results.map { it.diagnostics }
            .reduceOrNull(ExpectimaxDiagnostics::combineShared)

        return scoreResults to diagnostics
    }

    private suspend fun asyncComputeResults(req: ScoreRequest): List<ExpectimaxResult> =
        directions.map {
            scope.async {
                Expectimax(it, table)
                    .score(req.board, req.depthLimit)
            }
        }
            .awaitAll()
            .filterNotNull()

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
