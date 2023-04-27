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
    suspend fun findBestMove(board: Board4): FindBestMoveResult? {
        val distinctTiles = board.countDistinctTiles()
        val depthLimit = max(SPARSE_BOARD_MAX_DEPTH, distinctTiles - 2)

        val (pair, duration) = measureTimedValue {
            scoreAllDirections(ScoreRequest(board, depthLimit))
        }
        val (results, diagnostics) = pair
        val result = results.maxByOrNull { it.score } ?: return null

        return if (diagnostics != null) {
            logResults(duration, result.score, result.direction, diagnostics)

            FindBestMoveResult(
                result.direction,
                duration.inWholeMicroseconds / 1000f,
                diagnostics.maxDepth
            )
        } else
            null
    }

    protected abstract suspend fun scoreAllDirections(
        req: ScoreRequest,
    ): Pair<List<ScoreResult>, ExpectimaxDiagnostics?>

    private fun logResults(
        duration: Duration,
        score: Float,
        direction: Direction,
        d: ExpectimaxDiagnostics,
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
        data class ScoreResult(
            val score: Float,
            val direction: Direction,
        )

        data class FindBestMoveResult(
            val direction: Direction,
            val elapsedMs: Float,
            val maxDepth: Int,
        )

        data class ScoreRequest(
            val board: Board4,
            val depthLimit: Int,
        ) {
            fun serialize(): String =
                "${board.bits}|$depthLimit"

            companion object {
                fun deserialize(str: String?): ScoreRequest? {
                    if (str == null || str == "null")
                        return null

                    val (bits, depthLimit) = str.split("|")
                    return ScoreRequest(
                        Board4(bits.toULong()),
                        depthLimit.toInt()
                    )
                }
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
