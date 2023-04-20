package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.expectimax.impl.Board4Expectimax
import io.github.smaugfm.game2048.heuristics.impl.AnySizeBoardHeuristics
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.transposition.HashMapTranspositionTable
import io.github.smaugfm.game2048.transposition.ZobristHashTranspositionTable
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
    private var anySizeExpectimax = AnySizeExpectimax(AnySizeBoardHeuristics(), false)
    private var board4HashMapExpectimax =
        Board4Expectimax(Board4Heuristics(), HashMapTranspositionTable(), false)
    private var board4ZobristExpectimax =
        Board4Expectimax(Board4Heuristics(), ZobristHashTranspositionTable(), false)
    private var anySizeBoard = AnySizeBoard.fromArray(arr)
    private var board4 = Board4.fromArray(arr)

    @Benchmark
    fun anySize() {
        anySizeExpectimax.findBestMove(anySizeBoard)
    }

    @Benchmark
    fun board4HashMap() {
        board4HashMapExpectimax.findBestMove(board4)
    }

    @Benchmark
    fun board4Zobrist() {
        board4ZobristExpectimax.findBestMove(board4)
    }
}
