package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable
import kotlin.math.max

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class Expectimax internal constructor(
    private val dir: Direction,
    val transpositionTable: TranspositionTable
) {
    private var cacheSize: Int = 0
    private var evaluations: Long = 0
    private var moves: Long = 0
    private var cacheHits: Long = 0
    private var maxDepth: Int = 0
    private var depthLimit: Int = 0
    private val heuristics = Board4Heuristics()

    companion object {
        const val CACHE_DEPTH_LIMIT = 15
        const val PROBABILITY_THRESHOLD = 0.0001f// one in ten thousands
        const val SPARSE_BOARD_MAX_DEPTH = 3
    }

    fun score(board: Board4, depthLimit: Int): ExpectimaxResult? {
        val newBoard = board.move(dir)

        if (newBoard == board)
            return null
        this.depthLimit = depthLimit
        moves++

        val score = expectimaxNode(newBoard, 0, 1.0f).also {
            cacheSize = transpositionTable.size
        }

        return ExpectimaxResult(
            score,
            dir,
            object : ExpectimaxDiagnostics {
                override val cacheSize = this@Expectimax.cacheSize
                override val evaluations = this@Expectimax.evaluations
                override val moves = this@Expectimax.moves
                override val cacheHits = this@Expectimax.cacheHits
                override val maxDepth = this@Expectimax.maxDepth
                override val depthLimit = this@Expectimax.depthLimit
            }
        )
    }

    private fun expectimaxNode(
        board: Board4,
        depth: Int,
        prob: Float,
    ): Float {
        if (prob < PROBABILITY_THRESHOLD) {
            return evaluateBoard(depth, board)
        }
        if (depth >= depthLimit) {
            return evaluateBoard(depth, board)
        }
        val cachedScore = expectimaxCacheSearch(board, depth)
        if (cachedScore != null) {
            cacheHits++
            return cachedScore
        }

        val emptyCount = board.countEmptyTiles()
        val emptyTileProb = prob / emptyCount

        val score = run {
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
            sum
        } / emptyCount

        if (depth < CACHE_DEPTH_LIMIT) {
            transpositionTable.update(board, depth, score)
        }

        return score
    }

    private fun expectimaxCacheSearch(board: Board4, depth: Int): Float? {
        transpositionTable.search(board)?.let { entry ->
            if (entry.depth <= depth) {
                return entry.score
            }
        }
        return null
    }

    private fun moveNode(
        board: Board4,
        prob: Float,
        depth: Int,
    ): Float {
        return directions
            .map {
                val newBoard = board.move(it)
                moves++
                if (newBoard == board)
                    return@map Float.NEGATIVE_INFINITY
                expectimaxNode(newBoard, depth + 1, prob)
            }.max()
    }

    private fun evaluateBoard(depth: Int, board: Board4): Float {
        evaluations++
        maxDepth = max(depth, maxDepth)
        return heuristics.evaluate(board)
    }
}
