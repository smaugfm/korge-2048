package io.github.smaugfm.game2048.board.impl

import io.github.smaugfm.game2048.board.AnySizeBoard
import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.PrecomputedTables
import io.github.smaugfm.game2048.board.PrecomputedTables.unpackArray
import kotlin.test.Test
import kotlin.test.assertEquals

class PrecomputedTablesTest {
    @Test
    fun reverseLineTest() {
        assertEquals(0x123u.toUShort(), PrecomputedTables.reverseLine(0x3210u))
        assertEquals(0x0u.toUShort(), PrecomputedTables.reverseLine(0x0000u))
        assertEquals(0x1111u.toUShort(), PrecomputedTables.reverseLine(0x1111u))
        assertEquals(0x2211u.toUShort(), PrecomputedTables.reverseLine(0x1122u))
        assertEquals(0x2221u.toUShort(), PrecomputedTables.reverseLine(0x1222u))
    }

    @Test
    fun leftMovesTest() {
        movesTest(Direction.LEFT)
    }

    @Test
    fun rightMovesTest() {
        movesTest(Direction.RIGHT)
    }

    @Test
    fun upMovesTest() {
        movesTest(Direction.TOP)
    }

    @Test
    fun downMovesTest() {
        movesTest(Direction.BOTTOM)
    }

    private fun movesTest(dir: Direction) {
        for (line in (0u until 65536u)) {
            val lineArray = line.unpackArray()
            val arr = (lineArray + lineArray + lineArray + lineArray).toIntArray()

            val board4 = Board4.fromArray(arr)
            val board = AnySizeBoard.fromArray(arr)
            val newBoard = board.move(dir)
            newBoard.array.indices.forEach {
                if (newBoard.array[it] > 15)
                    newBoard.array[it] = 15
            }
            val actual = board4.move(dir)

            assertEquals(
                newBoard.tiles().toList(),
                actual.tiles().toList(),
                "initial\n$board4\n" +
                    "expected\n$newBoard\n" +
                    "actual\n $actual\n"
            )
        }
    }
}
