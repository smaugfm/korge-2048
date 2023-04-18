package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Board
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.heuristics.Heuristics
import korlibs.math.roundDecimalPlaces
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

abstract class Expectimax<T : Board<T>>(
    protected val heuristics: Heuristics<T>,
    private val log: Boolean = true
) {
    private var evaluations: Long = 0
    private var moves: Long = 0
    private var unprobable: Long = 0
    private var cacheHits: Long = 0
    private var cacheSize: Long = 0
    private var maxDepth: Int = 0
    private var depthLimit: Long = 0

    fun findBestDirection(board: T): Direction? {
        clearState()

        depthLimit = getDepthLimit(board).toLong()

        val (bestDirectionDescending, duration) =
            measureTimedValue {
                bestDirections(board)
            }

        cacheSize = getCurrentCacheSize()

        return bestDirectionDescending
            .firstOrNull { board.move(it.first) != board }
            .also { if (it != null) logResults(duration, it.first, it.second) }
            ?.first
    }

    private fun bestDirections(board: T): List<Pair<Direction, Float>> =
        directions.map { it to topLevelNode(board, it) }
            .sortedByDescending { it.second }

    private fun topLevelNode(
        board: T,
        it: Direction
    ): Float {
        val newBoard = board.move(it)
        moves++
        if (newBoard == board)
            return Float.NEGATIVE_INFINITY

        return expectimaxNode(newBoard, 0, 1.0f)
    }

    private fun expectimaxNode(
        board: T,
        depth: Int,
        prob: Float,
    ): Float {
        if (prob < PROBABILITY_THRESHOLD) {
            unprobable++
            return evaluateNode(depth, board)
        }
        if (depth >= depthLimit) {
            return evaluateNode(depth, board)
        }
        val cachedScore = expectimaxCacheSearch(board, depth)
        if (cachedScore != null) {
            cacheHits++
            return cachedScore
        }

        val emptyCount = board.countEmptyTiles()
        val emptyTileProb = prob / emptyCount

        val score = emptyTilesScoresSum(board, emptyTileProb, depth) / emptyCount

        expectimaxCacheStore(board, depth, score)

        return score
    }

    protected open fun expectimaxCacheStore(board: T, depth: Int, score: Float) {
        //do nothing
    }

    protected open fun expectimaxCacheSearch(board: T, depth: Int): Float? {
        return null
    }

    protected open fun getCurrentCacheSize(): Long = 0L


    private fun evaluateNode(depth: Int, board: T): Float {
        evaluations++
        maxDepth = max(depth, maxDepth)
        return heuristics.evaluate(board)
    }

    protected abstract fun emptyTilesScoresSum(
        board: T,
        emptyTileProb: Float,
        depth: Int,
    ): Float

    protected fun moveNode(
        board: T,
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

    protected open fun clearState() {
        evaluations = 0
        moves = 0
        unprobable = 0
        cacheHits = 0
        cacheSize = 0
        maxDepth = 0
        depthLimit = 0
    }

    open fun getDepthLimit(board: T): Int =
        SPARSE_BOARD_MAX_DEPTH

    private fun logResults(duration: Duration, direction: Direction, score: Float) {
        if (!log) return

        println(
            "Move $direction: score=${score.format()}, evaluated=${evaluations.format()}, " +
                "moves=${moves.format()}, unprobable=${unprobable.format()}, cacheHits=${cacheHits.format()}, " +
                "cacheSize=${cacheSize.format()}, maxDepth=$maxDepth in $duration"
        )
    }

    companion object {
        const val PROBABILITY_THRESHOLD = 0.0001f// one in ten thousands
        const val SPARSE_BOARD_MAX_DEPTH = 3

        fun Float.format(): String {
            return this.roundDecimalPlaces(2).toString()
        }

        fun Long.format(): String {
            if (this < 10_000)
                return this.toString()
            if (this < 1_000_000)
                return "${this / 1_000}k"
            if (this < 1_000_000_000)
                return "${this / 1_000_000}M"

            return "${this / 1_000_000_000}B"
        }
    }
}
