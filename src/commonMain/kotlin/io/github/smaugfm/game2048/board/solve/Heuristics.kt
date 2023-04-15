package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Board

interface Heuristics<T : Board<T>> {
    fun evaluate(board: T): Double
}
