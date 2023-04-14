package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AiTest {

    @Test
    fun test() {
        runBlocking {
            val board = Board(
                intArrayOf(
                    -1, -1, -1, 2,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                )
            )
            Ai.findBestMove(this, board).await()
        }
    }
}
