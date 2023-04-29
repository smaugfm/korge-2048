package io.github.smaugfm.game2048.util

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.search.SearchStats
import kotlin.math.pow
import kotlin.math.round
import kotlin.time.Duration

object FindBestMoveStatsLogger {

    fun logResults(
        duration: Duration,
        score: Float,
        depthLimit: Int,
        direction: Direction,
        s: SearchStats,
    ) {
        println(
            "Move ${direction.toString().padEnd(6)}: " +
                "score=${score.format(15)}, " +
                "evaluated=${s.evaluations.format(8)}, " +
                "moves=${s.moves.format(8)}, " +
                "cacheHits=${s.cacheHits.format(8)}, " +
                "cacheSize=${s.cacheSize.toLong().format(8)}, " +
                "maxDepth=${s.maxDepthReached.toString().padStart(2)}, " +
                "elapsed=$duration, " +
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
