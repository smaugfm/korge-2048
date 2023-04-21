package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable
import korlibs.io.async.runBlockingNoJs
import korlibs.io.concurrent.createFixedThreadDispatcher
import korlibs.math.roundDecimalPlaces
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.measureTimedValue

/**
 * Based on [this](https://github.com/nneonneo/2048-ai) repo
 */
class Expectimax(
    private val heuristics: Heuristics<Board4>,
    private val transpositionTable: TranspositionTable,
    private val scope: CoroutineScope? = null,
    private val log: Boolean = true,
) {
    private var cacheSize: Int = 0
    private var depthLimit: Long = 0

    fun findBestMove(board: Board4): Direction? {
        transpositionTable.clear()

        depthLimit = getDepthLimit(board).toLong()

        val (sortedResults, duration) =
            measureTimedValue {
                if (scope != null)
                    searchParallel(scope, board)
                else
                    search(board)
            }
        val state = sortedResults.state()
        cacheSize = transpositionTable.size

        return sortedResults
            .firstOrNull { board.move(it.direction) != board }
            .also { if (it != null) logResults(duration, it.direction, state, it.score) }
            ?.direction
    }

    private fun searchParallel(scope: CoroutineScope, board: Board4) =
        runBlockingNoJs {
            directions.map { d -> scope.async(dispatcher) { topLevelNode(board, d) } }
                .awaitAll()
                .sortedByDescending { it.score }
        }

    private fun search(board: Board4) =
        directions.map { d -> topLevelNode(board, d) }
            .sortedByDescending { it.score }

    private fun topLevelNode(
        board: Board4,
        dir: Direction
    ): ScoreResult {
        val state = DiagnosticState()
        val newBoard = board.move(dir)
        state.moves++
        if (newBoard == board)
            return ScoreResult(dir, state, Float.NEGATIVE_INFINITY)

        return ScoreResult(dir, state, expectimaxNode(state, newBoard, 0, 1.0f))
    }

    private fun expectimaxNode(
        state: DiagnosticState,
        board: Board4,
        depth: Int,
        prob: Float,
    ): Float {
        if (prob < PROBABILITY_THRESHOLD) {
            return evaluateBoard(state, depth, board)
        }
        if (depth >= depthLimit) {
            return evaluateBoard(state, depth, board)
        }
        val cachedScore = expectimaxCacheSearch(board, depth)
        if (cachedScore != null) {
            state.cacheHits++
            return cachedScore
        }

        val emptyCount = board.countEmptyTiles()
        val emptyTileProb = prob / emptyCount

        val score = run {
            var sum = 0.0f
            board.iterateEmptyTiles { tileIndex, _ ->
                val score2 = moveNode(
                    state,
                    board.placeTile(Tile.TWO, tileIndex),
                    emptyTileProb * TILE_TWO_PROBABILITY,
                    depth,
                )
                val score4 = moveNode(
                    state,
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
        state: DiagnosticState,
        board: Board4,
        prob: Float,
        depth: Int,
    ): Float {
        return directions
            .map {
                val newBoard = board.move(it)
                state.moves++
                if (newBoard == board)
                    return@map Float.NEGATIVE_INFINITY
                expectimaxNode(state, newBoard, depth + 1, prob)
            }.max()
    }

    private fun evaluateBoard(state: DiagnosticState, depth: Int, board: Board4): Float {
        state.evaluations++
        state.maxDepth = max(depth, state.maxDepth)
        return heuristics.evaluate(board)
    }

    private fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()
        return max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)
    }

    private fun logResults(
        duration: Duration,
        direction: Direction,
        state: DiagnosticState,
        score: Float
    ) {
        if (!log) return

        println(
            "Move ${direction.toString().padEnd(6)}: " +
                "score=${score.format(20)}, " +
                "evaluated=${state.evaluations.format(8)}, " +
                "moves=${state.moves.format(8)}, " +
                "cacheHits=${state.cacheHits.format(8)}, cacheSize=${
                    cacheSize.toLong().format(8)
                }, " +
                "maxDepth=${state.maxDepth.toString().padStart(2)}, " +
                "elapsed=$duration"
        )
    }

    companion object {
        private data class ScoreResult(
            val direction: Direction,
            val state: DiagnosticState,
            val score: Float
        )

        private class DiagnosticState(
            var evaluations: Long = 0,
            var moves: Long = 0,
            var cacheHits: Long = 0,
            var maxDepth: Int = 0,
        ) {
            operator fun plus(other: DiagnosticState): DiagnosticState =
                DiagnosticState(
                    evaluations + other.evaluations,
                    moves + other.moves,
                    cacheHits + other.cacheHits,
                    max(maxDepth, other.maxDepth)
                )
        }

        private fun List<ScoreResult>.state(): DiagnosticState =
            map { it.state }.reduce { acc, state -> state + acc }

        const val CACHE_DEPTH_LIMIT = 15
        const val PROBABILITY_THRESHOLD = 0.0001f// one in ten thousands
        const val SPARSE_BOARD_MAX_DEPTH = 3
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)

        fun Float.format(padStart: Int = 0): String {
            return this.roundDecimalPlaces(2).toString().padStart(padStart)
        }

        fun Long.format(padStart: Int = 0): String {
            val result = if (this < 10_000)
                this.toString()
            else if (this < 1_000_000)
                "${this / 1_000}k"
            else if (this < 1_000_000_000)
                "${(this / 1_000_000.0).roundDecimalPlaces(1)}M"
            else
                "${(this / 1_000_000_000.0).roundDecimalPlaces(2)}B"

            return result.padStart(padStart)
        }
    }
}
