package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction

object Ai {
    fun bestNextMove(board: Board): Direction {
        return Direction.values().random()
    }
}
