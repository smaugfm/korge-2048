package io.github.smaugfm.game2048.board

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.PrecomputedTables4
import io.github.smaugfm.game2048.board.impl.PrecomputedTables4.packArray
import io.github.smaugfm.game2048.board.impl.PrecomputedTables4.unpackArray
import kotlin.test.Test
import kotlin.test.assertEquals

class PrecomputedTables4Test {
    @Test
    fun reverseLineTest() {
        assertEquals(0x123u.toUShort(), PrecomputedTables4.reverseLine(0x3210u))
        assertEquals(0x0u.toUShort(), PrecomputedTables4.reverseLine(0x0000u))
        assertEquals(0x1111u.toUShort(), PrecomputedTables4.reverseLine(0x1111u))
        assertEquals(0x2211u.toUShort(), PrecomputedTables4.reverseLine(0x1122u))
        assertEquals(0x2221u.toUShort(), PrecomputedTables4.reverseLine(0x1222u))
    }

    @Test
    fun movesTest() {
        for (line in (0u until 65536u)) {
            val array = line.unpackArray()

            val board = AnySizeBoard.fromArray(array.toIntArray())
            val newBoard = AnySizeBoard.fromArray(intArrayOf(0, 0, 0, 0))
            board.moveLineToStart(intArrayOf(0, 1, 2, 3), newBoard)
            val expected = newBoard.array.toUIntArray().packArray().toUShort()

            assertEquals(
                expected,
                PrecomputedTables4.leftLinesTable[line.toInt()]
            )
        }
    }
}
