package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

actual class FindBestMoveImpl actual constructor(
    heuristics: Heuristics<Board4>,
    transpositionTableFactory: () -> TranspositionTable,
    log: Boolean
) : FindBestMove(heuristics, transpositionTableFactory, log) {

    override suspend fun executeScores(
        board: Board4,
        expectimaxList: List<Expectimax>
    ): List<Float?> =
        expectimaxList
            .map { scope.async { it.score(board) } }
            .awaitAll()

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
