package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax.Companion.SPARSE_BOARD_MAX_DEPTH
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration
import kotlin.time.measureTimedValue

abstract class FindBestMove protected constructor(
    private val log: Boolean = true,
) {
    suspend fun findBestMove(board: Board4): Direction? {
        val distinctTiles = board.countDistinctTiles()
        val depthLimit = max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)

        val (results, duration) = measureTimedValue {
            scoreAllDirections(ScoreRequest(board, depthLimit))
                .filterNotNull()
        }
        val result = results.firstOrNull() ?: return null
        val combinedDiagnostics =
            results.map { it.diagnostics }.reduce { acc, d -> acc + d }

        logResults(duration, result.score, result.direction, combinedDiagnostics)

        return result.direction
    }

    protected abstract suspend fun scoreAllDirections(
        req: ScoreRequest
    ): List<ExpectimaxResult?>

    private fun logResults(
        duration: Duration,
        score: Float,
        direction: Direction,
        d: ExpectimaxDiagnostics
    ) {
        if (!log) return

        println(
            "Move ${direction.toString().padEnd(6)}: " +
                "score=${score.format(20)}, " +
                "evaluated=${d.evaluations.format(8)}, " +
                "moves=${d.moves.format(8)}, " +
                "cacheHits=${d.cacheHits.format(8)}, cacheSize=${
                    d.cacheSize.toLong().format(8)
                }, " +
                "maxDepth=${d.maxDepth.toString().padStart(2)}, " +
                "elapsed=$duration, " +
                "depthLimit=${d.depthLimit}"
        )
    }

    companion object {

        data class ScoreRequest(
            val board: Board4,
            val depthLimit: Int
        ) {
            fun toMap() =
                mapOf(
                    ScoreRequest::board.name to board.bits.toString(),
                    ScoreRequest::depthLimit.name to depthLimit.toString()
                )

            companion object {
                fun fromMap(map: Map<String, String>): ScoreRequest =
                    ScoreRequest(
                        Board4(map[ScoreRequest::board.name]!!.toULong()),
                        map[ScoreRequest::depthLimit.name]!!.toInt()
                    )
            }
        }

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
