package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreResult
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove(log) {
    private val table = ConcurrentHashMapTranspositionTable()

    override suspend fun scoreAllDirections(
        requests: List<ScoreRequest>,
    ): Pair<List<ScoreResult>, ExpectimaxDiagnostics?> {
        table.clear()

        val results = asyncComputeResults(requests)

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

    private suspend fun asyncComputeResults(requests: List<ScoreRequest>): List<ExpectimaxResult> =
        requests.map {
            scope.async { Expectimax(table).score(it) }
        }
            .awaitAll()
            .filterNotNull()

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
