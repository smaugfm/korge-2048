package io.github.smaugfm.game2048.heuristics

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Board4.Companion.ROW_MASK
import io.github.smaugfm.game2048.board.PrecomputedTables

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class Board4Heuristics : Heuristics<Board4> {
    private val heuristicsTable = PrecomputedTables.heuristicsTable

    override fun evaluate(board: Board4): Float =
        scoreLines(board) + scoreLines(board.transpose())

    private fun scoreLines(b: Board4): Float =
        heuristicsTable[((b.bits shr 0) and ROW_MASK).toInt()] +
            heuristicsTable[((b.bits shr 16) and ROW_MASK).toInt()] +
            heuristicsTable[((b.bits shr 32) and ROW_MASK).toInt()] +
            heuristicsTable[((b.bits shr 48) and ROW_MASK).toInt()]
}
