package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.util.fastRepeat
import kotlin.math.abs
import kotlin.math.min

/**
 * Based on [this](https://azaky.github.io/2048-AI/paper.pdf) paper
 */
class AzakyAnySizeHeuristics : Heuristics<AnySizeBoard> {
    companion object {
        private const val EMPTY_WEIGHT = 4096
        private const val SMOOTHNESS_WEIGHT = 10
        private const val BORDER_DISTANCE_WEIGHT = 10
    }

    override fun evaluate(board: AnySizeBoard): Double {
        var empty = 0
        var diffs = 0
        var distances = 0

        fastRepeat(boardSize) { row ->
            fastRepeat(boardSize) { col ->
                val tile = board[row, col]
                if (tile.isEmpty) empty++
                val borderDistance =
                    min(min(row, boardSize - 1 - row), min(col, boardSize - 1 - col))
                distances += tile.score * borderDistance
                if (col >= 1) {
                    val left = board[row, col - 1]
                    diffs += abs(tile.score - left.score)
                }
                if (row >= 1) {
                    val up = board[row - 1, col]
                    diffs += abs(tile.score - up.score)
                }
            }
        }

        return (empty * EMPTY_WEIGHT -
            diffs * SMOOTHNESS_WEIGHT -
            distances * BORDER_DISTANCE_WEIGHT
            ).toDouble()
    }
}
