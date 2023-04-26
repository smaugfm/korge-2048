package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax.Companion.SPARSE_BOARD_MAX_DEPTH
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.measureTimedValue

abstract class FindBestMove protected constructor(
    private val heuristics: Heuristics<Board4>,
    private val transpositionTableFactory: () -> TranspositionTable,
    private val log: Boolean = true,
) {
    private var cacheSize: Int = 0
    private val tables =
        directions.map { transpositionTableFactory() }

    suspend fun findBestMove(board: Board4): Direction? {
        tables.forEach { it.clear() }

        val distinctTiles = board.countDistinctTiles()
        val depthLimit = max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)

        val (result, duration) = measureTimedValue {
            computeScore(depthLimit, board) ?: return null
        }

        logResults(duration, result)

        return result.direction
    }

    private suspend fun FindBestMove.computeScore(
        depthLimit: Int,
        board: Board4
    ): ScoreResult? {
        val expectimaxList = directions.mapIndexed { i, dir ->
            Expectimax(heuristics, tables[i], dir, depthLimit)
        }

        val (i, score) = executeScores(
            board,
            expectimaxList
        ).mapIndexedNotNull { i, score ->
            score?.let {
                i to score
            }
        }
            .maxByOrNull { it.second } ?: return null

        return ScoreResult(
            directions[i],
            expectimaxList[i],
            score
        )
    }

    protected abstract suspend fun executeScores(
        board: Board4,
        expectimaxList: List<Expectimax>
    ): List<Float?>

    data class ScoreResult(
        val direction: Direction,
        val state: ExpectimaxDiagnostics,
        val score: Float
    )

    private fun logResults(
        duration: Duration,
        result: ScoreResult,
    ) {
        if (!log) return

        val (direction, state, score) = result

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

        fun Float.format(padStart: Int = 0): String =
            this.roundDecimalPlaces(2).toString().padStart(padStart)

        private fun Float.roundDecimalPlaces(places: Int): Float {
            if (places < 0) return this
            val placesFactor: Float = 10f.pow(places.toFloat())
            return round(this * placesFactor) / placesFactor
        }

        private fun Double.roundDecimalPlaces(places: Int): Double {
            if (places < 0) return this
            val placesFactor: Double = 10.0.pow(places.toDouble())
            return round(this * placesFactor) / placesFactor
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
