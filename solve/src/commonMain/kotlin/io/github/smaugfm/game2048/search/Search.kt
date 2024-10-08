package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Direction
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
            SearchStatsLogger.logResults(
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

    protected abstract suspend fun calculateBoardScore(
        board: Board4,
        depthLimit: Int,
    ): List<SearchResult>

    private fun getDepthLimit(board: Board4): Int {
        val distinctTiles = board.countDistinctTiles()

        return max(MIN_DEPTH_LIMIT, platformDepthLimit(distinctTiles))
    }

    protected abstract suspend fun init()
    protected abstract fun platformDepthLimit(distinctTiles: Int): Int

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
