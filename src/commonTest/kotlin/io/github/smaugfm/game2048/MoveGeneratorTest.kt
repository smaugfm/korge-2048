package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveGenerator
import org.junit.Test
import kotlin.test.*

class MoveGeneratorTest {
    @Test
    fun testMap1() {
        val board = Board(
            intArrayOf(
                2, 2, -1, 3,
                2, 3, -1, -1,
                1, 2, 3, -1,
                1, 2, 3, -1
            )
        )
        val (board1, moves1) = MoveGenerator.moveBoard(board, Direction.LEFT)
        assertEquals(
            Board(
                intArrayOf(
                    3, 3, -1, -1,
                    2, 3, -1, -1,
                    1, 2, 3, -1,
                    1, 2, 3, -1
                )
            ), board1
        )

        val (board2, moves2) = MoveGenerator.moveBoard(board1, Direction.TOP)
        assertEquals(
            Board(
                intArrayOf(
                    3, 4, 4, -1,
                    2, 3, -1, -1,
                    2, -1, -1, -1,
                    -1, -1, -1, -1
                )
            ), board2
        )
        println(moves1)
        println(moves2)
    }

    @Test
    fun testMoveLine0() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 2, 3, -1))
        assertEquals(posMapOneLine(intArrayOf(1, 2, 3, -1)), newBoard)
    }

    @Test
    fun testMoveLine1() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine2() {
        val newBoard = genPosMapForOneLine(intArrayOf(2, 1, -1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine3() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 1, 1, -1))
        assertEquals(posMapOneLine(intArrayOf(2, 1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine4() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, -1, -1, -1))
        assertEquals(posMapOneLine(intArrayOf(1, -1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine5() {
        val newBoard = genPosMapForOneLine(intArrayOf(-1, -1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, -1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine6() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, -1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine7() {
        val newBoard = genPosMapForOneLine(intArrayOf(4, 3, 2, 1))
        assertEquals(posMapOneLine(intArrayOf(4, 3, 2, 1)), newBoard)
    }

    @Test
    fun testMoveLine8() {
        val newBoard = genPosMapForOneLine(intArrayOf(4, 3, 2, 1))
        assertEquals(posMapOneLine(intArrayOf(4, 3, 2, 1)), newBoard)
    }

    @Test
    fun testMoveLine9() {
        val newBoard = genPosMapForOneLine(intArrayOf(-1, -1, -1, -1))
        assertEquals(posMapOneLine(intArrayOf(-1, -1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine10() {
        val newBoard = genPosMapForOneLine(intArrayOf(-1, -1, -1, 1))
        assertEquals(posMapOneLine(intArrayOf(1, -1, -1, -1)), newBoard)
    }

    @Test
    fun testMoveLine11() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 2, 3, 4))
        assertEquals(posMapOneLine(intArrayOf(1, 2, 3, 4)), newBoard)
    }

    private fun posMapOneLine(array: IntArray) =
        Board(array + IntArray(boardArraySize - array.size) { -1 })

    private fun genPosMapForOneLine(array: IntArray): Board {
        val board = posMapOneLine(array)
        val newBoard = Board()
        val moves = mutableListOf<MoveGenerator.BoardMove>()
        MoveGenerator.moveLine(
            listOf(0, 1, 2, 3),
            board,
            newBoard,
            moves
        )
        return newBoard
    }
}
