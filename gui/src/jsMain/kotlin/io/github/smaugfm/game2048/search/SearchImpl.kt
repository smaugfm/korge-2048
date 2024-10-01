package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction.Companion.directions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.w3c.dom.Worker
import kotlin.math.max

actual class SearchImpl actual constructor(log: Boolean) : Search() {
    private val usingWasm = false
    private val workers =
        directions.associateWith {
            Worker(
                if (usingWasm)
                    "./wasm.js"
                else
                    "./js.js"
            )
        }

    actual override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - if (usingWasm) 3 else 6

    actual override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult> =
        requests.map(::webWorkerSearch)
            .awaitAll()
            .filterNotNull()

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

    private fun webWorkerSearch(req: SearchRequest): Deferred<SearchResult?> =
        workers[req.dir]!!.computeScore(req.serialize())

    companion object {
        fun Worker.computeScore(data: String): Deferred<SearchResult?> {
            val completableDeferred = CompletableDeferred<SearchResult?>()
            this.onmessage = { messageEvent ->
                val result = SearchResult.deserialize(messageEvent.data.toString())
                completableDeferred.complete(result)
            }
            this.onerror = { event ->
                completableDeferred.completeExceptionally(RuntimeException(event.type))
            }
            this.postMessage(data)

            return completableDeferred
        }
    }
}
