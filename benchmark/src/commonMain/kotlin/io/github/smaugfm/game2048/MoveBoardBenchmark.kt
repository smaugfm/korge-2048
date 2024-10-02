package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Direction
import kotlinx.benchmark.*

@State(Scope.Benchmark)
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
