package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.solve.Expectimax
import io.github.smaugfm.game2048.board.solve.Heuristics
import kotlinx.benchmark.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope

@DelicateCoroutinesApi
@State(Scope.Benchmark)
@Measurement(
    iterations = 10,
    time = 1,
    timeUnit = BenchmarkTimeUnit.SECONDS
)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
class ExpectimaxBenchmark {
    private var expectimax = Expectimax(EmptyHeuristics, false)
    private var board: AnySizeBoard = AnySizeBoard(
        intArrayOf(
            2, 2, -1, 3,
            2, 3, 1, -1,
            1, 1, 1, 1,
            1, -1, -1, 1
        )
    )

    private object EmptyHeuristics : Heuristics<AnySizeBoard> {
        override fun evaluate(board: AnySizeBoard): Double {
            return 0.0
        }
    }

    @Benchmark
    fun evaluate() {
        expectimax.findBestMove(board)
    }
}
