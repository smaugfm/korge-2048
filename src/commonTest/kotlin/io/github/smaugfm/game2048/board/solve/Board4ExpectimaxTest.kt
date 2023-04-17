package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.expectimax.impl.Board4Expectimax
import korlibs.datastructure.random.FastRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class Board4ExpectimaxTest {

    @Test
    fun test() {
        repeat(10_000) {
            val score = FastRandom.nextFloat()
            val depth = FastRandom.nextInt()
            val entry = Board4Expectimax.CacheEntry.create(score, depth)

            assertEquals(score, entry.score)
            assertEquals(depth, entry.depth)
        }
    }
}
