package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Board4
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
        checkWasmSupport()
        workers = if (usingWasm == true) {
            consoleLogBold("Using WebAssembly Expectimax implementation")
            loadWorkers("./wasmJs.js")
        } else {
            consoleLogBold("Falling back to Javascript Expectimax implementation")
            loadWorkers("./js.js")
        }
    }

    actual override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - if (usingWasm == true) 3 else 6

    actual override suspend fun calculateBoardScore(
        board: Board4,
        depthLimit: Int
    ): List<SearchResult> =
        Direction.entries.map { SearchRequest(board, depthLimit, it) }.map(::webWorkerSearch)
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

        suspend fun checkWasmSupport() {
            val deferred = CompletableDeferred<Unit>()

            val worker = Worker("./wasmJs.js")
            try {
                withTimeout(200) {
                    worker.onmessage = { e ->
                        if (e.data.toString() == "started")
                            deferred.complete(Unit)
                        else
                            console.log(
                                "Message received from wasm " +
                                    "worker is not 'started' but ${e.data.toString()}"
                            )
                    }
                    delay(100)
                    deferred.await()
                    usingWasm = true
                }
            } catch (e: TimeoutCancellationException) {
                usingWasm = false
            }
            worker.terminate()
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
