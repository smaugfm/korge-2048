package io.github.smaugfm.game2048.heuristics

import io.github.smaugfm.game2048.board.Board

interface Heuristics<T : Board<T>> {
    fun evaluate(board: T): Double
}
