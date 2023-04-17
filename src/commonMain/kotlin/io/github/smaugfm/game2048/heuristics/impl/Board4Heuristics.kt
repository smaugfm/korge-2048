package io.github.smaugfm.game2048.heuristics.impl

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.board.impl.PrecomputedTables4
import io.github.smaugfm.game2048.heuristics.Heuristics

class Board4Heuristics : Heuristics<Board4> {
    private val heuristicsTable = PrecomputedTables4.heuristicsTable

    override fun evaluate(board: Board4): Float {
        return scoreLines(board) + scoreLines(board.transpose())
    }

    private fun scoreLines(b: Board4): Float =
        heuristicsTable[b.firstRow] +
            heuristicsTable[b.secondRow] +
            heuristicsTable[b.thirdRow] +
            heuristicsTable[b.fourthRow]

    companion object {
    }
}
