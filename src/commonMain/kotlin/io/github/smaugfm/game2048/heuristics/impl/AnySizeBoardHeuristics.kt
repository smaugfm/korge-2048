package io.github.smaugfm.game2048.heuristics.impl

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.boardSize
import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.AnySizeBoard.Companion.directionIndexesMap
import io.github.smaugfm.game2048.heuristics.Heuristics
import kotlin.math.min
import kotlin.math.pow

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class AnySizeBoardHeuristics : Heuristics<AnySizeBoard> {
    override fun evaluate(board: AnySizeBoard): Float {
        var sum = 0.0f
        repeat(boardSize) {
            val row = evaluateLine(board, directionIndexesMap[Direction.LEFT.ordinal][it])
            val top = evaluateLine(board, transposeIndexesMap[it])
            sum += row + top
        }
        return sum
    }

    fun evaluateLine(board: AnySizeBoard, indexes: IntArray): Float {
        var empty = 0

        var merges = 0
        var rowMerges = 0

        var prevTile: Tile? = null
        var sum = 0.0f
        var monoDec = 0.0f
        var monoInc = 0.0f

        for (index in indexes) {
            val tile = Tile(board.array[index])
            if (tile.isEmpty) {
                empty++
            } else {
                sum += tile.power.toFloat().pow(SUM_POW)
                if (prevTile == tile) {
                    rowMerges++
                } else if (rowMerges > 0) {
                    merges += 1 + rowMerges
                    rowMerges = 0
                }
                if (prevTile != null) {
                    if (prevTile.power > tile.power) {
                        monoDec +=
                            ((prevTile.power.toFloat())
                                .pow(MONO_POW) - tile.power.toFloat().pow(MONO_POW))
                    } else {
                        monoInc += (tile.power.toFloat()
                            .pow(MONO_POW) - prevTile.power.toFloat().pow(MONO_POW))
                    }
                }

            }
            prevTile = tile
        }
        if (rowMerges > 0)
            merges += 1 + rowMerges

        return UNCONDITIONAL_ADDEND +
            EMPTY_WEIGHT * empty +
            MERGES_WEIGHT * merges -
            MONO_WEIGHT * min(monoDec, monoInc) -
            SUM_WEIGHT * sum
    }

    companion object {
        const val UNCONDITIONAL_ADDEND = 200000.0f;
        const val SUM_POW = 3.5f
        const val SUM_WEIGHT = 11.0f
        const val MONO_POW = 4.0f
        const val MONO_WEIGHT = 47.0f
        const val MERGES_WEIGHT = 700.0f
        const val EMPTY_WEIGHT = 270.0f

        private val transposeIndexesMap = (0 until boardSize).map { row ->
            (0 until boardSize).map { col ->
                col * boardSize + row
            }.toIntArray()
        }.toTypedArray()
    }
}
