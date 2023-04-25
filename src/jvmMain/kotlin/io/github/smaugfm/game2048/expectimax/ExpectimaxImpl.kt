package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

actual class ExpectimaxImpl internal actual constructor(
    heuristics: Heuristics<Board4>,
    transpositionTable: TranspositionTable,
    log: Boolean
) : Expectimax(heuristics, transpositionTable, log) {
    override suspend fun search(board: Board4): List<ScoreResult> =
        directions.map { d -> scope.async(dispatcher) { topLevelNode(board, d) } }
            .awaitAll()
            .sortedByDescending { it.score }

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
