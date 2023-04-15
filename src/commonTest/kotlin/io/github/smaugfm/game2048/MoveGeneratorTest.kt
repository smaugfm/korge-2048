package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.optimized.Board4
import io.github.smaugfm.game2048.board.AnySizeBoard
import org.junit.Test
import kotlin.test.assertEquals

class MoveGeneratorTest {
    @Test
    fun testMap1() {
        val board = Board4.fromArray(
            intArrayOf(
                2, 2, 0, 3,
                2, 3, 0, 0,
                1, 2, 3, 0,
                1, 2, 3, 0
            )
        )
        val (board1, moves1) = board.moveBoardGenerateMoves(Direction.LEFT)
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

        val (board2, moves2) = board.moveBoardGenerateMoves(Direction.TOP)
        assertEquals(
            Board4.fromArray(
                intArrayOf(
                    3, 4, 4, 0,
                    2, 3, 0, 0,
                    2, 0, 0, 0,
                    0, 0, 0, 0
                )
            ), board2
        )
        println(moves1)
        println(moves2)
    }

    @Test
    fun testMoveLine0() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 2, 3, 0))
        assertEquals(posMapOneLine(intArrayOf(1, 2, 3, 0)), newBoard)
    }

    @Test
    fun testMoveLine1() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine2() {
        val newBoard = genPosMapForOneLine(intArrayOf(2, 1, 0, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine3() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 1, 1, 0))
        assertEquals(posMapOneLine(intArrayOf(2, 1, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine4() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 0, 0, 0))
        assertEquals(posMapOneLine(intArrayOf(1, 0, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine5() {
        val newBoard = genPosMapForOneLine(intArrayOf(0, 0, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 0, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine6() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 0, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 1, 0, 0)), newBoard)
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
        val newBoard = genPosMapForOneLine(intArrayOf(0, 0, 0, 0))
        assertEquals(posMapOneLine(intArrayOf(0, 0, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine10() {
        val newBoard = genPosMapForOneLine(intArrayOf(0, 0, 0, 1))
        assertEquals(posMapOneLine(intArrayOf(1, 0, 0, 0)), newBoard)
    }

    @Test
    fun testMoveLine11() {
        val newBoard = genPosMapForOneLine(intArrayOf(1, 2, 3, 4))
        assertEquals(posMapOneLine(intArrayOf(1, 2, 3, 4)), newBoard)
    }

    private fun posMapOneLine(array: IntArray) =
        AnySizeBoard(array + IntArray(boardArraySize - array.size) { Tile.EMPTY.power })

    private fun genPosMapForOneLine(array: IntArray): AnySizeBoard {
        val board = posMapOneLine(array)
        val newBoard = AnySizeBoard()
        val moves = mutableListOf<BoardMove>()
        board.moveLineLeftGenerateMoves(
            intArrayOf(0, 1, 2, 3),
            newBoard,
            moves
        )
        return newBoard
    }
}
