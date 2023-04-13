package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveGenerator
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 10,
    time = 1,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class MoveGeneratorBenchmark {
    private lateinit var board: Board

    @Setup
    fun setup() {
        board = Board(
            intArrayOf(
                2, 2, -1, 3,
                2, 3, 1, -1,
                1, 1, 1, 1,
                1, -1, -1, 1
            )
        )
    }

    @Benchmark
    fun moveBoard(bh: Blackhole) {
        bh.consume(
            MoveGenerator.moveBoard(board, Direction.LEFT, { _, _ -> }, { _, _, _ -> })
        )
    }
}
