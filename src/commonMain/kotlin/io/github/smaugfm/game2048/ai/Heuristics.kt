package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.core.GeneralBoard

object Heuristics {
    private val weights = intArrayOf(
        50, 30, 20, 20,
        30, 20, 15, 15,
        15, 5, 0, 0,
        -5, -5, -10, -15
    )

    fun evaluate(board: GeneralBoard): Long {
        var i = 0
        var sum = 0L
        while (i < boardSize) {
            sum += board[i].power * weights[i]
            i++
        }

        return sum
    }

    fun monotonicityHeuristic(board: GeneralBoard): Long {
        var best = 0L

        (right(board) + down(board)).let {
            if (it > best)
                best = it
        }

        (left(board) + up(board)).let {
            if (it > best)
                best = it
        }
        (right(board) + up(board)).let {
            if (it > best)
                best = it
        }
        (left(board) + down(board)).let {
            if (it > best)
                best = it
        }

        return best
    }

    //1 --->
    //2 --->
    //3 --->
    //4 --->
    private fun right(board: GeneralBoard): Long {
        var current = 0L
        for (row in (0 until boardSize)) {
            for (col in (0 until boardSize - 1)) {
                val a = board[row, col].power
                val b = board[row, col + 1].power
                if (a > b) {
                    current++
                }
            }
        }
        return current
    }

    //1 <---
    //2 <---
    //3 <---
    //4 <---
    private fun left(board: GeneralBoard): Long {
        var current = 0L
        for (row in (0 until boardSize)) {
            for (col in (0 until boardSize - 1)) {
                val a = board[row, col].power
                val b = board[row, col + 1].power
                if (a < b) {
                    current++
                }

            }
        }
        return current
    }

    //1 2 3 4
    //| | | |
    //| | | |
    //| | | |
    //↓ ↓ ↓ ↓
    private fun down(board: GeneralBoard): Long {
        var current = 0L
        for (col in (0 until boardSize)) {
            for (row in (0 until boardSize - 1)) {
                val a = board[row, col].power
                val b = board[row + 1, col].power
                if (a > b) {
                    current++
                }
            }
        }
        return current
    }

    //1 2 3 4
    //↑ ↑ ↑ ↑
    //| | | |
    //| | | |
    //| | | |
    private fun up(board: GeneralBoard): Long {
        var current = 0L
        for (col in (0 until boardSize)) {
            for (row in (0 until boardSize - 1)) {
                val a = board[row, col].power
                val b = board[row + 1, col].power
                if (a < b) {
                    current++
                }
            }
        }
        return current
    }

    private fun smoothnessHeuristic(board: GeneralBoard): Long {
        TODO("Not yet implemented")
    }

    private fun emptyTilesHeuristic(board: GeneralBoard): Long {
        TODO("Not yet implemented")
    }
}
