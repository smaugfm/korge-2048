package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlin.math.max

actual class SearchImpl actual constructor(log: Boolean) : Search(log) {
    private val table = ConcurrentHashMapTranspositionTable()

    override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - 2

    override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult> {
        table.clear()

        return requests.map(::threadedSearch)
            .awaitAll()
            .filterNotNull()
    }

    override fun combineStats(
        one: SearchStats,
        two: SearchStats,
    ) = object : SearchStats {
        override val cacheSize: Int get() = one.cacheSize
        override val evaluations = one.evaluations + two.evaluations
        override val moves = one.moves + two.moves
        override val cacheHits = one.cacheHits + two.cacheHits
        override val maxDepthReached = max(
            one.maxDepthReached,
            two.maxDepthReached
        )
    }

    private fun threadedSearch(req: SearchRequest) =
        scope.async {
            ExpectimaxSearch(table).score(req)
        }

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
