package io.github.smaugfm.game2048.expectimax.impl

import LongLongMap
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import kotlin.jvm.JvmInline
import kotlin.math.max

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class Board4Expectimax(heuristics: Heuristics<Board4>, log: Boolean = true) :
    Expectimax<Board4>(heuristics, log) {

    private var searchCache = LongLongMap()

    @JvmInline
    value class CacheEntry(val bits: Long) {
        val score get() = Float.fromBits(bits.toInt())
        val depth get() = (bits ushr 32).toInt()

        companion object {
            fun create(score: Float, depth: Int): CacheEntry =
                CacheEntry(
                    (score.toRawBits().toULong() or (depth.toULong() shl 32)).toLong()
                )
        }
    }

    override fun clearState() {
        super.clearState()
        searchCache.clear()
    }

    override fun getCurrentCacheSize(): Long =
        searchCache.size.toLong()

    override fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()
        return max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)
    }

    override fun expectimaxCacheStore(board: Board4, depth: Int, score: Float) {
        if (depth >= CACHE_DEPTH_LIMIT)
            return

        searchCache[board.bits.toLong()] = CacheEntry.create(score, depth).bits
    }

    override fun expectimaxCacheSearch(board: Board4, depth: Int): Float? {
        val bits = searchCache[board.bits.toLong()]
        if (bits == 0L) return null

        val entry = CacheEntry(bits)
        if (entry.depth <= depth)
            return entry.score

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
