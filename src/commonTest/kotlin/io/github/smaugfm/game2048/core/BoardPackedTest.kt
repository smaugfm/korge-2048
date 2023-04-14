package io.github.smaugfm.game2048.core

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
        val board = Board4(arr)

        assertEquals(
            0x3333222211110000.toULong(), board.packed
        )
    }
}
