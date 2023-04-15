package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.boardSize
import kotlin.math.abs

class SleepyCoderAnySizeHeuristic : Heuristics<AnySizeBoard> {
    override fun evaluate(board: AnySizeBoard): Double {
        var diff = 0
        var sum = 0
        var row = 0
        var i = 0
        var empty = 0

        while (row < boardSize) {
            var col = 0
            while (col < boardSize) {
                val tile = board[i]
                val score = tile.score
                if (tile.isEmpty)
                    empty++
                sum += score
                if (col < 3) {
                    val right = board[row, col + 1]
                    diff += abs(score - right.score)
                }
                if (row < 3) {
                    val down = board[row + 1, col]
                    diff += abs(score - down.score)
                }
                col++
                i++
            }
            row++
        }
        return if (empty == 0)
            Double.NEGATIVE_INFINITY
        else
            ((sum * 4 - diff) * 2).toDouble()
    }
}
