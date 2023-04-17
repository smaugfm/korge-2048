package io.github.smaugfm.game2048.expectimax.impl

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import kotlin.math.max

class Board4Expectimax(heuristics: Heuristics<Board4>, log: Boolean = true) :
    Expectimax<Board4>(heuristics, log) {
    override fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()
        return max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)
    }

    override fun emptyTilesScoresSum(
        board: Board4,
        emptyTileProb: Float,
        depth: Int,
    ): Double {
        var sum = 0.0
        board.iterateEmptyTiles { tileIndex, _ ->
            val score2 = moveNode(
                board.placeTile(Tile.TWO, tileIndex),
                emptyTileProb * TILE_TWO_PROBABILITY,
                depth,
            )
            val score4 = moveNode(
                board.placeTile(Tile.FOUR, tileIndex),
                emptyTileProb * TILE_FOUR_PROBABILITY,
                depth,
            )

            sum += score2 * TILE_TWO_PROBABILITY + score4 * TILE_FOUR_PROBABILITY
        }

        return sum
    }
}
