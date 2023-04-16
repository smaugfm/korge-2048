package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import kotlinx.benchmark.*
import kotlinx.coroutines.DelicateCoroutinesApi

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
