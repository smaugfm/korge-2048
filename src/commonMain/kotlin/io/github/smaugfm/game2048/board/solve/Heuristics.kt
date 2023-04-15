package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Board

interface Heuristics<T : Board<T>> {
    fun evaluate(board: T): Double

    companion object {
        const val SCORE_POW = 3.5
        const val SCORE_WEIGHT = 11
        const val MONO_POW = 4.0
        const val MONO_WEIGHT = 47.0
        const val MERGES_WEIGHT = 700.0
        const val EMPTY_WEIGHT = 270.0
        const val LOST_PENALTY = -200000.0
    }
}
