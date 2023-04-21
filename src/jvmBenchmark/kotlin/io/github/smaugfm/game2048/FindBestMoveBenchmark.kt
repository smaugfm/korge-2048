package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import io.github.smaugfm.game2048.transposition.ZobristHashTranspositionTable
import kotlinx.benchmark.*
import kotlin.random.Random

@State(Scope.Benchmark)
@Measurement(
    iterations = 2,
    time = 10,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@Warmup(
    iterations = 2,
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
    private var board4HashMapExpectimax =
        Expectimax(Board4Heuristics(), HashMapTranspositionTable(), false)
    private var board4ZobristExpectimax =
        Expectimax(Board4Heuristics(), ZobristHashTranspositionTable(), false)
    private var board4 = Board4.fromArray(arr)

    @Benchmark
    fun board4HashMap() {
        board4HashMapExpectimax.findBestMove(board4)
    }

    @Benchmark
    fun board4Zobrist() {
        board4ZobristExpectimax.findBestMove(board4)
    }
}
