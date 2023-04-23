package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import korlibs.io.async.runBlockingNoJs
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 5,
    time = 10,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@Warmup(
    iterations = 5,
    time = 10,
    timeUnit = BenchmarkTimeUnit.SECONDS
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
    private var expectimax =
        Expectimax.create(
            Board4Heuristics(),
            ConcurrentHashMapTranspositionTable(),
            false
        )
    private var board4 = Board4.fromArray(arr)

    @Benchmark
    fun board4parallelConcurrentHashMapExpectimax() {
        runBlockingNoJs {
            expectimax.findBestMove(board4)
        }
    }
}
