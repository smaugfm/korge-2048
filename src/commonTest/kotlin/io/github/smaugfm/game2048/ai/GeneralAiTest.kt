package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.GeneralBoard
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GeneralAiTest {

    @Test
    fun test() {
        runBlocking {
            val board = GeneralBoard(
                intArrayOf(
                    -1, -1, -1, 2,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                    -1, -1, -1, -1,
                )
            )
            GeneralAi.findBestMove(this, board).await()
        }
    }
}
