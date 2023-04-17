package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.expectimax.impl.Board4Expectimax
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.heuristics.impl.NneonneoAnySizeHeuristics
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
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
    private var anySizeExpectimax = AnySizeExpectimax(NneonneoAnySizeHeuristics(), false)
    private var board4Expectimax = Board4Expectimax(Board4Heuristics(), false)
    private var anySizeBoard = AnySizeBoard.fromArray(arr)
    private var board4 = Board4.fromArray(arr)

    @Benchmark
    fun anySize() {
        anySizeExpectimax.findBestDirection(anySizeBoard)
    }

    @Benchmark
    fun board4() {
        board4Expectimax.findBestDirection(board4)
    }
}
