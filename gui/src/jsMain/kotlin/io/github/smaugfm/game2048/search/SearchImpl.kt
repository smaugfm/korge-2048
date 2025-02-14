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
        val wasmCodeFile = "./wasmJs.js"
        val jsCodeFile = "./js.js"

        usingWasm = checkWebWorkers(wasmCodeFile)

        if (usingWasm == true) {
            consoleLogBold("Using WebAssembly Expectimax implementation")
            workers = loadWorkers(wasmCodeFile)
        } else if (checkWebWorkers(jsCodeFile)) {
            consoleLogBold("Falling back to Javascript Expectimax implementation")
            workers = loadWorkers(jsCodeFile)
        } else {
            println("Web workers were not loaded. AI is disabled.")
        }
    }

    actual override fun platformDepthLimit(distinctTiles: Int) =
        distinctTiles - if (usingWasm == true) 3 else 6

    actual override suspend fun calculateBoardScore(
        board: Board4,
        depthLimit: Int
    ): List<SearchResult> =
        Direction.entries.map { SearchRequest(board, depthLimit, it) }
            .map(::webWorkerSearch)
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

        suspend fun checkWebWorkers(codeFile: String): Boolean {
            val deferred = CompletableDeferred<Unit>()

            var workersLoaded = false
            val worker = Worker(codeFile)
            try {
                withTimeout(200) {
                    worker.onmessage = { e ->
                        if (e.data.toString() == "pong") {
                            deferred.complete(Unit)
                        } else
                            console.log(
                                "Message received from web worker " +
                                    "is not 'pong' but ${e.data.toString()}"
                            )
                    }
                    delay(100)
                    worker.postMessage("ping")
                    deferred.await()
                    workersLoaded = true
                }
            } catch (e: TimeoutCancellationException) {
                workersLoaded = false
            }
            worker.terminate()
            return workersLoaded
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
