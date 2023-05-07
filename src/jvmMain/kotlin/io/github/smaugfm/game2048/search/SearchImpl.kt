package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.transposition.Long2LongMapTranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.actor
import kotlin.math.max

@OptIn(ObsoleteCoroutinesApi::class)
actual class SearchImpl actual constructor(log: Boolean) : Search(log) {
    private val workers =
        directions.associateWith {
            scope.actor<WorkerMessage> {
                val table = Long2LongMapTranspositionTable()
                for (msg in channel) {
                    try {
                        msg.result.complete(
                            ExpectimaxSearch(table).score(msg.req)
                        )
                    } catch (e: Throwable) {
                        println(
                            "Unhandled exception in worker (jvm):"
                        )
                        println(e)
                        msg.result.completeExceptionally(e)
                    }
                }
            }
        }

    class WorkerMessage(
        val req: SearchRequest,
        val result: CompletableDeferred<SearchResult?>,
    )

    override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - 2

    override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult> {
        return requests.map { threadedSearch(it) }
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun threadedSearch(req: SearchRequest): Deferred<SearchResult?> =
        CompletableDeferred<SearchResult?>().also {
            workers[req.dir]!!.send(WorkerMessage(req, it))
        }

    override fun combineStats(
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
            Dispatchers.createFixedThreadDispatcher(
                "expectimax",
                Direction.values().size
            )
        private val scope = CoroutineScope(dispatcher)
    }
}
