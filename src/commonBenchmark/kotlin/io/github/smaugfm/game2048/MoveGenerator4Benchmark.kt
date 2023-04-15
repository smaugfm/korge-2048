package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.four.Board4
import kotlinx.benchmark.*

@State(Scope.Benchmark)
@Measurement(
    iterations = 10,
    time = 1,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class MoveGenerator4Benchmark {
    private var board: Board4 = Board4.fromArray(
        intArrayOf(
            2, 2, -1, 3,
            2, 3, 1, -1,
            1, 1, 1, 1,
            1, -1, -1, 1
        )
    )

    @Benchmark
    fun moveBoard(bh: Blackhole) {
        bh.consume(
            MoveGenerator4.moveBoard(board, Direction.LEFT)
        )
    }
}
