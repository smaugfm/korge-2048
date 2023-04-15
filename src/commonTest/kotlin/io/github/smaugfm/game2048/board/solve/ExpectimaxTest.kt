package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ExpectimaxTest {

    @Test
    fun test() {
        runBlocking {
            val board = AnySizeBoard(
                intArrayOf(
                    -1, -1, -1, 2,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                )
            )
            Expectimax(AnySizeBoardHeuristics())
                .findBestMove(this, board).await()
        }
    }
}
