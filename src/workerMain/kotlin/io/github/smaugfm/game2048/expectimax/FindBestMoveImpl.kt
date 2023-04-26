package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable

actual class FindBestMoveImpl actual constructor(
    heuristics: Heuristics<Board4>,
    transpositionTableFactory: () -> TranspositionTable,
    log: Boolean
) : FindBestMove(heuristics, transpositionTableFactory, log) {
    override suspend fun executeScores(
        board: Board4,
        expectimaxList: List<Expectimax>
    ): List<Float?> {
        throw UnsupportedOperationException()
    }
}
