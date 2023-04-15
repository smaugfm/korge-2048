package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.Board
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.boardSize

interface Heuristics<T : Board<T>> {
    fun evaluate(board: T): Double
    fun evaluateLine(board: T, indexes: IntArray): Double {
        throw UnsupportedOperationException()
    }

    companion object {
    }
}
