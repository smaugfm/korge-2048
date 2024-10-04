package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.transposition.Long2LongMapTranspositionTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.concurrent.Executors
import kotlin.math.max

actual class SearchImpl actual constructor(log: Boolean) : Search(log) {
    public actual override suspend fun init() {
        //do nothing
    }

    actual override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - 2

    actual override suspend fun calculateBoardScore(
        board: Board4,
        depthLimit: Int
    ): List<SearchResult> =
        Direction.entries.map { SearchRequest(board, depthLimit, it) }.map { req ->
            scope.async {
                val table = Long2LongMapTranspositionTable()
                ExpectimaxSearch(table).score(req)
            }
        }.awaitAll().filterNotNull()

    actual override fun combineStats(
        one: SearchStats,
        two: SearchStats,
    ) = object : SearchStats {
        override val cacheSize: Int get() = one.cacheSize + two.cacheSize
        override val evaluations = one.evaluations + two.evaluations
        override val moves = one.moves + two.moves
        override val cacheHits = one.cacheHits + two.cacheHits
        override val maxDepthReached = max(
            one.maxDepthReached,
            two.maxDepthReached
        )
    }

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Executors.newFixedThreadPool(Direction.entries.size).asCoroutineDispatcher()
        private val scope = CoroutineScope(dispatcher)
    }
}
