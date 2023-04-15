package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.AnySizeBoard.Companion.directionIndexesMap
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.boardSize
import kotlin.math.min
import kotlin.math.pow

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class NneonneoAnySizeHeuristics : Heuristics<AnySizeBoard> {
    override fun evaluate(board: AnySizeBoard): Double =
        (0 until boardSize).sumOf { i ->
            val left = evaluateLine(board, directionIndexesMap[Direction.LEFT.ordinal][i])
            val top = evaluateLine(board, directionIndexesMap[Direction.TOP.ordinal][i])
            left + top
        }


    override fun evaluateLine(board: AnySizeBoard, indexes: IntArray): Double {
        var empty = 0
        var merges = 0
        var score = 0.0

        var prevTile: Tile? = null
        var rowMerges = 0
        var monoDec = 0.0
        var monoInc = 0.0

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
                        monoDec +=
                            ((prevTile.power.toDouble())
                                .pow(MONO_POW) - tile.power.toDouble().pow(MONO_POW))
                    } else {
                        monoInc += (tile.power.toDouble()
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
            min(monoDec, monoInc) + SCORE_WEIGHT * score
    }

    companion object {
        const val SCORE_POW = 3.5
        const val SCORE_WEIGHT = 11
        const val MONO_POW = 6.0
        const val MONO_WEIGHT = 47.0
        const val MERGES_WEIGHT = 700.0
        const val EMPTY_WEIGHT = 270.0
        const val LOST_PENALTY = Double.NEGATIVE_INFINITY
    }
}
