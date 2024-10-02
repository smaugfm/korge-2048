package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.search.ExpectimaxSearch
import io.github.smaugfm.game2048.search.SearchRequest
import io.github.smaugfm.game2048.transposition.Long2LongMapTranspositionTable
import org.w3c.dom.DedicatedWorkerGlobalScope

private fun getWorkerGlobalScope(): DedicatedWorkerGlobalScope =
  js("self")

fun main() {
  val table = Long2LongMapTranspositionTable()
  val self = getWorkerGlobalScope()
  println("Web-worker (wasm) started")

  self.onmessage = { messageEvent ->
    try {
      val requestStr = messageEvent.data.toString()
      if (requestStr == "ping")
        self.postMessage("pong".toJsString())
      else {
        val request = SearchRequest.deserialize(requestStr)!!

        val scoreResult = ExpectimaxSearch(table).score(request)
        self.postMessage(
          scoreResult
            ?.serialize()
            ?.toJsString()
        )
      }
    } catch (e: Throwable) {
      println("Unhandled exception in web worker (wasm):")
      println(e)
    }
  }
}
