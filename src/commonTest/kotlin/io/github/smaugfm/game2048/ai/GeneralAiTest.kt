package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.board.AnySizeBoard
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GeneralAiTest {

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
            Expectimax.findBestMove(this, board).await()
        }
    }
}
