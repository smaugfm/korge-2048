package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.AnySizeBoard.Companion.directionIndexesMap
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.EMPTY_WEIGHT
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.LOST_PENALTY
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.MERGES_WEIGHT
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.MONO_POW
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.MONO_WEIGHT
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.SCORE_POW
import io.github.smaugfm.game2048.board.solve.Heuristics.Companion.SCORE_WEIGHT
import io.github.smaugfm.game2048.boardSize
import kotlin.math.min
import kotlin.math.pow

class AnySizeBoardHeuristics : Heuristics<AnySizeBoard> {
    override fun evaluate(board: AnySizeBoard): Double =
        (0 until boardSize).sumOf { i ->
            evaluateLine(board, directionIndexesMap[Direction.LEFT.ordinal][i]) +
                evaluateLine(board, directionIndexesMap[Direction.TOP.ordinal][i])
        }


    fun evaluateLine(board: AnySizeBoard, indexes: IntArray): Double {
        var empty = 0
        var merges = 0
        var score = 0.0

        var prevTile: Tile? = null
        var rowMerges = 0
        var monoLeft = 0.0
        var monoRight = 0.0

        for (index in indexes) {
            val tile = Tile(board.array[index])
            if (tile.isEmpty) {
                empty++
            } else {
                score += tile.power.toDouble().pow(SCORE_POW)
                if (prevTile == tile) {
                    rowMerges++
                } else if (rowMerges > 0) {
                    merges += 1 + rowMerges
                    rowMerges = 0
                }
                if (prevTile != null) {
                    if (prevTile.power > tile.power) {
                        monoLeft +=
                            (prevTile.power.toDouble()
                                .pow(MONO_POW) - tile.power.toDouble().pow(MONO_POW))
                    } else {
                        monoRight += (tile.power.toDouble()
                            .pow(MONO_POW) - prevTile.power.toDouble().pow(MONO_POW))
                    }
                }

            }
            prevTile = tile
        }
        if (rowMerges > 0)
            merges += 1 + rowMerges

        if (empty == 0 && merges == 0)
            return LOST_PENALTY

        return EMPTY_WEIGHT * empty + MERGES_WEIGHT * merges + MONO_WEIGHT *
            min(monoLeft, monoRight) + SCORE_WEIGHT * score
    }
}
