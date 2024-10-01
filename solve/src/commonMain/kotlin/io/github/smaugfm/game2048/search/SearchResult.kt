package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction

data class SearchResult(
    val score: Float,
    val direction: Direction,
    val stats: SearchStats,
) {
    fun serialize(): String {
        val res = "$score|$direction"
        val diag = with(stats) {
            "$cacheSize|$evaluations|$moves|$cacheHits|$maxDepthReached"
        }

        return "$res|$diag"
    }

    companion object {
        fun deserialize(str: String?): SearchResult? {
            if (str == null || str == "null")
                return null
            val s = str.split("|")

            return SearchResult(
                s[0].toFloat(),
                Direction.valueOf(s[1]),
                object : SearchStats {
                    override val cacheSize = s[2].toInt()
                    override val evaluations = s[3].toLong()
                    override val moves = s[4].toLong()
                    override val cacheHits = s[5].toLong()
                    override val maxDepthReached = s[6].toInt()
                }
            )
        }
    }
}
