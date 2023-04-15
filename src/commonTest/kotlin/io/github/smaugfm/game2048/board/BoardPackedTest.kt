package io.github.smaugfm.game2048.board

import io.github.smaugfm.game2048.board.optimized.Board4
import org.junit.Test
import kotlin.test.assertEquals

class BoardPackedTest {

    @Test
    fun test() {
        val arr = intArrayOf(
            -1, -1, -1, -1,
            1, 1, 1, 1,
            2, 2, 2, 2,
            3, 3, 3, 3,
        )
        val board = Board4.fromArray(arr)

        assertEquals(
            0x3333222211110000.toULong(), board.packed
        )
    }
}
