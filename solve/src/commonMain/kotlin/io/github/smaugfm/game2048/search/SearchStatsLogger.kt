package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration

object SearchStatsLogger {
    private val elapsedRegex = "(\\d+)(?:\\.\\d+)?(\\w+)".toRegex()

    fun logResults(
        duration: Duration,
        score: Float,
        depthLimit: Int,
        direction: Direction,
        s: SearchStats,
    ) {
        val m = elapsedRegex.matchEntire(duration.toString())!!
        val elapsed = "${m.groupValues[1].padStart(3)}${m.groupValues[2].padEnd(2)}"

        println(
            "Move ${direction.toString().padEnd(6)}: " +
                "score=${score.format(12)}, " +
                "elapsed=${elapsed}, " +
                "maxDepth=${s.maxDepthReached.toString().padStart(2)}, " +
                "evals=${s.evaluations.format(6)}, " +
                "moves=${s.moves.format(6)}, " +
                "cacheHits=${s.cacheHits.format(6)}, " +
                "cacheSize=${s.cacheSize.toLong().format(6)}, " +
                "depthLimit=$depthLimit"
        )
    }

    private fun Float.format(padStart: Int = 0): String =
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

    private fun Long.format(padStart: Int = 0): String {
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
