package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.AnySizeBoard
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 10,
    time = 1,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class AnySizeBoardBenchmark {
    private lateinit var board: AnySizeBoard

    @Setup
    fun setup() {
        board = AnySizeBoard(
            intArrayOf(
                2, 2, -1, 3,
                2, 3, 1, -1,
                1, 1, 1, 1,
                1, -1, -1, 1
            )
        )
    }

    @Benchmark
    fun moveBoard() {
        board.move(Direction.LEFT)
    }
}
