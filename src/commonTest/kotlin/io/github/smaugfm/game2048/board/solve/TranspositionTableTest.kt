package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.transposition.TranspositionTable
import io.github.smaugfm.game2048.transposition.ZobristHashTranspositionTable
import korlibs.datastructure.random.FastRandom
import kotlin.test.Test
import kotlin.test.assertEquals

class TranspositionTableTest {

    @Test
    fun test() {
        repeat(10_000) {
            val score = FastRandom.nextFloat()
            val depth = FastRandom.nextInt()
            val entry = TranspositionTable.CacheEntry.create(score, depth)

            assertEquals(score, entry.score)
            assertEquals(depth, entry.depth)
        }
    }
}
