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
        while (row < boardSize) {
            var col = 0
            while (col < boardSize) {
                sum += board[i].score
                if (col < 3)
                    diff += abs(board[row, col].score - board[row, col + 1].score)
                if (row < 3)
                    diff += abs(board[row, col].score - board[row + 1, col].score)
                col++
                i++
            }
            row++
        }
        return ((sum * 4 - diff) * 2).toDouble()
    }
}
