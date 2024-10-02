package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.usingWasm
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.w3c.dom.Worker
import kotlin.math.max

actual class SearchImpl actual constructor(log: Boolean) : Search() {
    private lateinit var workers: Map<Direction, Worker>

    private fun loadWorkers(srcCode: String): Map<Direction, Worker> =
        directions.associateWith { Worker(srcCode) }

    public actual override suspend fun init() {
        workers = loadWorkers("./wasmJs.js")

        //Without this pingPongCheck's do not succeed for some reason
        delay(100)

        if (workers.values.all { it.pingPongCheck() }) {
            usingWasm = true
            consoleLogBold("Using WebAssembly Expectimax implementation")
        } else {
            usingWasm = false
            consoleLogBold("Falling back to Javascript Expectimax implementation")
            workers = loadWorkers("./js.js")
            if (workers.values.any { !it.pingPongCheck() }) {
                consoleLogBold("JS web worker did not load correctly")
            }
        }
    }

    actual override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - if (usingWasm == true) 3 else 6

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
        fun consoleLogBold(str: String) {
            console.log(
                "%c$str%c",
                "font-weight: bold",
                "font-weight: normal"
            )
        }

        suspend fun Worker.pingPongCheck(): Boolean {
            val res = CompletableDeferred<Boolean>()
            onmessage = { e ->
                if (e.data.toString() == "pong")
                    res.complete(true)
                else {
                    console.log(e.data)
                    res.complete(false)
                }
            }
            onerror = { e ->
                console.error(e)
                res.complete(false)
            }
            postMessage("ping")
            return try {
                withTimeout(50) {
                    res.await()
                }
            } catch (e: TimeoutCancellationException) {
                console.log("Timed out waiting for 'pong' from wasm worker")
                false
            }
        }

        fun Worker.computeScore(data: String): Deferred<SearchResult?> {
            val res = CompletableDeferred<SearchResult?>()
            onmessage = { e ->
                val result = SearchResult.deserialize(e.data.toString())
                res.complete(result)
            }
            onerror = { e ->
                res.completeExceptionally(RuntimeException(e.type))
            }
            postMessage(data)

            return res
        }
    }
}
