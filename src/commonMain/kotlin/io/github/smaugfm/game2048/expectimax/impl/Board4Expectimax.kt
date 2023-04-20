package io.github.smaugfm.game2048.expectimax.impl

import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import kotlin.math.max

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
open class Board4Expectimax(
    heuristics: Heuristics<Board4>,
    log: Boolean = true
) : Expectimax<Board4>(heuristics, log) {

    private var transpositionTable = TranspositionTable()

    override fun clearState() {
        super.clearState()
        transpositionTable.clear()
    }

    override fun getCurrentCacheSize() =
        transpositionTable.size

    override fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()
        return max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)
    }

    override fun expectimaxCacheStore(board: Board4, depth: Int, score: Float) {
        if (depth >= CACHE_DEPTH_LIMIT)
            return

        transpositionTable.update(board, depth, score)
    }

    override fun expectimaxCacheSearch(board: Board4, depth: Int): Float? {
        transpositionTable.search(board) { cacheDepth, score ->
            if (cacheDepth != null && score != null && cacheDepth <= depth) {
                return score
            }
        }
        return null
    }

    override fun emptyTilesScoresSum(
        board: Board4,
        emptyTileProb: Float,
        depth: Int,
    ): Float {
        var sum = 0.0f
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

    companion object {
        const val CACHE_DEPTH_LIMIT = 15
    }
}
