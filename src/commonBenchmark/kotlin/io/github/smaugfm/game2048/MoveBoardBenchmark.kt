package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.Board4
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
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class MoveBoardBenchmark {
    private val arr =
        intArrayOf(
            2, 2, 0, 3,
            2, 3, 1, 0,
            1, 1, 1, 1,
            1, 0, 0, 1
        )

    private var board4 = Board4.fromArray(arr)
    private var anySizeBoard = AnySizeBoard.fromArray(arr)

    @Benchmark
    fun anySize() {
        anySizeBoard.move(Direction.LEFT)
    }

    @Benchmark
    fun board4() {
        board4.move(Direction.LEFT)
    }
}
