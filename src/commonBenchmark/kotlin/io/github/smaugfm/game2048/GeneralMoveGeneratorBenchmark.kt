package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.general.GeneralBoard
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 10,
    time = 1,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class GeneralMoveGeneratorBenchmark {
    private lateinit var board: GeneralBoard

    @Setup
    fun setup() {
        board = GeneralBoard(
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
            GeneralMoveGenerator.moveBoard(board, Direction.LEFT)
        )
    }
}
