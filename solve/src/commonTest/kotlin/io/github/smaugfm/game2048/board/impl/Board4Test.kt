package io.github.smaugfm.game2048.board.impl

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.util.toMatrixString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class Board4Test {
    @Test
    fun transposeTest() {
        val board = Board4.fromArray(
            intArrayOf(
                0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                12, 13, 14, 15
            )
        )
        val transposed = Board4.fromArray(
            intArrayOf(
                0, 4, 8, 12,
                1, 5, 9, 13,
                2, 6, 10, 14,
                3, 7, 11, 15
            )
        )

        assertEquals(transposed, board.transpose())
    }

    private fun Board4.countDistinctTilesSimple(): Int {
        return tiles().toList()
            .filter { it.isNotEmpty }
            .distinctBy { it.power }
            .size
    }

    @Test
    fun countDistinctTilesTest() {
        repeat(10_000) {
            val b = Board4(Random.nextLong().toULong())
            assertEquals(b.countDistinctTilesSimple(), b.countDistinctTiles())
        }
    }

    @Test
    fun fromArrayTest() {
        val arr = intArrayOf(
            -1, -1, -1, -1,
            1, 1, 1, 1,
            2, 2, 2, 2,
            3, 3, 3, 3,
        )
        val board = Board4.fromArray(arr)

        assertEquals(
            0x3333222211110000.toULong(), board.bits
        )
    }

    @Test
    fun placeRandomBlockTest() {
        val board = Board4.fromArray(
            intArrayOf(
                0, 2, 0, 0,
                1, 2, 0, 0,
                0, 2, 1, 0,
                0, 2, 0, 0,
            )
        )

        repeat(1000) {
            val (newBoard, power, index) = board.placeRandomTile() ?: fail()
            assertTrue {
                board[index] == 0UL
            }
            assertTrue {
                power == Tile.TWO || power == Tile.FOUR
            }
            assertTrue(
                "try #$it:\n board:\n${board.toIntArray().toMatrixString(4)}\n" +
                    "newboard:\n${newBoard.toIntArray().toMatrixString(4)}"
            ) {
                ((newBoard.bits xor board.bits) shr (index * 4)) or 0xFUL == 0xFUL
            }
        }
    }

    @Test
    fun movesTest() {
        val board = Board4.fromArray(
            intArrayOf(
                2, 2, 0, 3,
                2, 3, 0, 0,
                1, 2, 3, 0,
                1, 2, 3, 0
            )
        )
        val (board1, moves1) = board.moveGenerateMoves(Direction.LEFT)
        assertEquals(
            Board4.fromArray(
                intArrayOf(
                    3, 3, 0, 0,
                    2, 3, 0, 0,
                    1, 2, 3, 0,
                    1, 2, 3, 0
                )
            ), board1
        )

        val (board2, moves2) = board1.moveGenerateMoves(Direction.BOTTOM)
        assertEquals(
            Board4.fromArray(
                intArrayOf(
                    0, 0, 0, 0,
                    3, 0, 0, 0,
                    2, 4, 0, 0,
                    2, 3, 4, 0
                )
            ), board2
        )
        println(moves1)
        println(moves2)
    }
}
