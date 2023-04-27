package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction

data class ExpectimaxResult(
    val score: Float,
    val direction: Direction,
    val diagnostics: ExpectimaxDiagnostics,
) {
    fun serialize(): String {
        val res = "$score|$direction"
        val diag = with(diagnostics) {
            "$cacheSize|$evaluations|$moves|$cacheHits|$maxDepth|$depthLimit"
        }

        return "$res|$diag"
    }

    companion object {
        fun deserialize(str: String?): ExpectimaxResult? {
            if (str == null || str == "null")
                return null
            val s = str.split("|")

            return ExpectimaxResult(
                s[0].toFloat(),
                Direction.valueOf(s[1]),
                object : ExpectimaxDiagnostics {
                    override val cacheSize = s[2].toInt()
                    override val evaluations = s[3].toLong()
                    override val moves = s[4].toLong()
                    override val cacheHits = s[5].toLong()
                    override val maxDepth = s[6].toInt()
                    override val depthLimit = s[7].toInt()
                }
            )
        }
    }
}
