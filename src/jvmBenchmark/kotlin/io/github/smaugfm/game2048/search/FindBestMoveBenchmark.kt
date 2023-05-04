package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.impl.Board4
import korlibs.io.async.runBlockingNoJs
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 2, time = 10, timeUnit = BenchmarkTimeUnit.SECONDS
)
@Warmup(
    iterations = 2, time = 10, timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
class FindBestMoveBenchmark {
    private val arr =
        intArrayOf(
            2, 2, 0, 3,
            2, 3, 1, 0,
            1, 1, 1, 1,
            1, 0, 0, 1
        )
    private var search = SearchImpl(false)
    private var board4 = Board4.fromArray(arr)

    @Benchmark
    fun expectimax() =
        runBlockingNoJs {
            search.findBestMove(board4)
        }
}
