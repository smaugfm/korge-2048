package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction

data class ExpectimaxResult(
    val score: Float,
    val direction: Direction,
    val diagnostics: ExpectimaxDiagnostics,
) {
    fun toMap(): Map<String, Any> =
        mapOf(
            ExpectimaxResult::score.name to score.toString(),
            ExpectimaxResult::direction.name to direction.toString(),
            ExpectimaxResult::diagnostics.name to mapOf(
                ExpectimaxDiagnostics::cacheSize.name to diagnostics.cacheSize,
                ExpectimaxDiagnostics::evaluations.name to diagnostics.evaluations,
                ExpectimaxDiagnostics::moves.name to diagnostics.moves,
                ExpectimaxDiagnostics::cacheHits.name to diagnostics.cacheHits,
                ExpectimaxDiagnostics::maxDepth.name to diagnostics.maxDepth,
                ExpectimaxDiagnostics::depthLimit.name to diagnostics.depthLimit,
            )
        )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>?): ExpectimaxResult? =
            map?.let {
                val diagnostics =
                    it[ExpectimaxResult::diagnostics.name] as Map<String, String>
                ExpectimaxResult(
                    it[ExpectimaxResult::score.name].toString().toFloat(),
                    Direction.valueOf(it[ExpectimaxResult::direction.name].toString()),
                    object : ExpectimaxDiagnostics {
                        override val cacheSize: Int =
                            diagnostics[ExpectimaxDiagnostics::cacheSize.name].toString()
                                .toInt()
                        override val evaluations: Long =
                            diagnostics[ExpectimaxDiagnostics::evaluations.name].toString()
                                .toLong()
                        override val moves: Long =
                            diagnostics[ExpectimaxDiagnostics::moves.name].toString()
                                .toLong()
                        override val cacheHits: Long =
                            diagnostics[ExpectimaxDiagnostics::cacheHits.name].toString()
                                .toLong()
                        override val maxDepth: Int =
                            diagnostics[ExpectimaxDiagnostics::maxDepth.name].toString()
                                .toInt()
                        override val depthLimit: Int =
                            diagnostics[ExpectimaxDiagnostics::depthLimit.name].toString()
                                .toInt()
                    }
                )
            }
    }
}
