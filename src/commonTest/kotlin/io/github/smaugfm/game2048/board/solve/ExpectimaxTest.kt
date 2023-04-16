package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import korlibs.io.async.runBlockingNoJs
import kotlin.test.Test

class ExpectimaxTest {

    @Test
    fun test() {
        runBlockingNoJs {
            val board = AnySizeBoard(
                intArrayOf(
                    -1, -1, -1, 2,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                )
            )
            Expectimax(NneonneoAnySizeHeuristics())
                .findBestMove(this, board).await()
        }
    }
}
