package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.util.FindBestMoveStatsLogger
import kotlin.math.max
import kotlin.time.measureTimedValue

abstract class Search protected constructor(
    private val log: Boolean = true,
) {
    suspend fun findBestMove(board: Board4): FindBestMoveResult? {
        val depthLimit = getDepthLimit(board)

        val (results, duration) = measureTimedValue {
            calculateBoardScore(board, depthLimit)
        }

        val bestResult = results.maxByOrNull { it.score } ?: return null
        val combinedStats = results.map { it.stats }.reduce(::combineStats)

        if (log)
            FindBestMoveStatsLogger.logResults(
                duration,
                bestResult.score,
                depthLimit,
                bestResult.direction,
                combinedStats
            )

        return FindBestMoveResult(
            bestResult.direction,
            duration.inWholeMicroseconds / 1000f,
            combinedStats.maxDepthReached
        )
    }

    private suspend fun Search.calculateBoardScore(
        board: Board4,
        depthLimit: Int,
    ) = getExpectimaxResults(
        directions.map {
            SearchRequest(
                board,
                depthLimit,
                it
            )
        })

    private fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()

        return max(MIN_DEPTH_LIMIT, distinctTiles - distinctTilesDepthNegativeTerm)
    }

    protected abstract val distinctTilesDepthNegativeTerm: Int

    protected abstract suspend fun getExpectimaxResults(
        requests: List<SearchRequest>,
    ): List<SearchResult>

    protected abstract fun combineStats(
        one: SearchStats,
        two: SearchStats,
    ): SearchStats

    companion object {
        const val MIN_DEPTH_LIMIT = 3

        data class FindBestMoveResult(
            val direction: Direction,
            val elapsedMs: Float,
            val maxDepth: Int,
        )

    }

}
