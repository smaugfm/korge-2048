package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board

interface Heuristics<T : Board<T>> {
    fun evaluate(board: T): Long
}
