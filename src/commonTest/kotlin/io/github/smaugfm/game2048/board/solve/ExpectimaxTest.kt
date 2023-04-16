package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.impl.NneonneoAnySizeHeuristics
import kotlin.test.Test

class ExpectimaxTest {
    @Test
    fun test() {
        val board = AnySizeBoard(
            intArrayOf(
                -1, -1, -1, 2,
                -1, -1, -1, -1,
                -1, -1, -1, -1,
                -1, -1, -1, -1,
            )
        )
        Expectimax(NneonneoAnySizeHeuristics())
            .findBestMove(board)
    }
}
