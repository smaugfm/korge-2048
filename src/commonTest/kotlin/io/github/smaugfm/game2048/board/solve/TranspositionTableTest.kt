package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.transposition.TranspositionTable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TranspositionTableTest {

    @Test
    fun test() {
        repeat(10_000) {
            val score = Random.nextFloat()
            val depth = Random.nextInt()
            val entry = TranspositionTable.CacheEntry.create(score, depth)

            assertEquals(score, entry.score)
            assertEquals(depth, entry.depth)
        }
    }
}
