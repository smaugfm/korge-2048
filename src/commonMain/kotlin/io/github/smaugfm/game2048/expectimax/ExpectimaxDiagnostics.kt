package io.github.smaugfm.game2048.expectimax

import kotlin.math.max

interface ExpectimaxDiagnostics {
    val cacheSize: Int
    val evaluations: Long
    val moves: Long
    val cacheHits: Long
    val maxDepth: Int
    val depthLimit: Int

    operator fun plus(other: ExpectimaxDiagnostics): ExpectimaxDiagnostics {
        val that = this@ExpectimaxDiagnostics
        return object : ExpectimaxDiagnostics {
            override val cacheSize: Int get() = that.cacheSize + other.cacheSize
            override val evaluations = that.evaluations + other.evaluations
            override val moves = that.moves + other.moves
            override val cacheHits = that.cacheHits + other.cacheHits
            override val maxDepth = max(that.maxDepth, other.maxDepth)
            override val depthLimit = max(that.depthLimit, other.depthLimit)
        }
    }
}
