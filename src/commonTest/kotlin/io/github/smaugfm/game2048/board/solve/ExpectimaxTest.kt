package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.heuristics.impl.NneonneoAnySizeHeuristics
import kotlin.test.Test

class ExpectimaxTest {
    @Test
    fun test() {
        val board = AnySizeBoard.fromArray(
            intArrayOf(
                0, 1, 1, 2,
                0, 1, 1, 1,
                0, 1, 1, 1,
                0, 0, 0, 0,
            )
        )
        AnySizeExpectimax(NneonneoAnySizeHeuristics())
            .findBestDirection(board)
    }
}
