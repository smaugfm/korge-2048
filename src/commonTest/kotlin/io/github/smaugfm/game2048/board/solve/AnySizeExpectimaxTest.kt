package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.heuristics.impl.AnySizeBoardHeuristics
import kotlin.test.Test

class AnySizeExpectimaxTest {
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
        AnySizeExpectimax(AnySizeBoardHeuristics())
            .findBestDirection(board)
    }
}
