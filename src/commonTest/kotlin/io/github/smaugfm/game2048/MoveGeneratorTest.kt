package io.github.smaugfm.game2048

import org.junit.Test
import kotlin.test.*

class MoveGeneratorTest {
    @Test
    fun testMap1() {
        val map = PositionMap(
            intArrayOf(
                2, 2, -1, 3,
                2, 3, -1, -1,
                1, 2, 3, -1,
                1, 2, 3, -1
            )
        )
        val (map1, _) = MoveGenerator.moveMap(map, Direction.LEFT)
        assertEquals(
            PositionMap(
                intArrayOf(
                    3, 3, -1, -1,
                    2, 3, -1, -1,
                    1, 2, 3, -1,
                    1, 2, 3, -1
                )
            ), map1
        )

        val (map2, _) = MoveGenerator.moveMap(map1, Direction.TOP)
        assertEquals(
            PositionMap(
                intArrayOf(
                    3, 4, 4, -1,
                    2, 3, -1, -1,
                    2, -1, -1, -1,
                    -1, -1, -1, -1
                )
            ), map2
        )
    }

    @Test
    fun testMoveLine1() {
        val newM = genPosMapForOneLine(intArrayOf(1, 1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, -1, -1)), newM)
    }

    @Test
    fun testMoveLine2() {
        val newM = genPosMapForOneLine(intArrayOf(2, 1, -1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 2, -1, -1)), newM)
    }

    @Test
    fun testMoveLine3() {
        val newM = genPosMapForOneLine(intArrayOf(1, 1, 1, -1))
        assertEquals(posMapOneLine(intArrayOf(2, 1, -1, -1)), newM)
    }

    @Test
    fun testMoveLine4() {
        val newM = genPosMapForOneLine(intArrayOf(1, -1, -1, -1))
        assertEquals(posMapOneLine(intArrayOf(1, -1, -1, -1)), newM)
    }

    @Test
    fun testMoveLine5() {
        val newM = genPosMapForOneLine(intArrayOf(-1, -1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, -1, -1, -1)), newM)
    }

    @Test
    fun testMoveLine6() {
        val newM = genPosMapForOneLine(intArrayOf(1, -1, 1, 1))
        assertEquals(posMapOneLine(intArrayOf(2, 1, -1, -1)), newM)
    }

    @Test
    fun testMoveLine7() {
        val newM = genPosMapForOneLine(intArrayOf(4, 3, 2, 1))
        assertEquals(posMapOneLine(intArrayOf(4, 3, 2, 1)), newM)
    }

    @Test
    fun testMoveLine8() {
        val newM = genPosMapForOneLine(intArrayOf(4, 3, 2, 1))
        assertEquals(posMapOneLine(intArrayOf(4, 3, 2, 1)), newM)
    }

    @Test
    fun testMoveLine9() {
        val newM = genPosMapForOneLine(intArrayOf(-1, -1, -1, -1))
        assertEquals(posMapOneLine(intArrayOf(-1, -1, -1, -1)), newM)
    }

    @Test
    fun testMoveLine10() {
        val newM = genPosMapForOneLine(intArrayOf(-1, -1, -1, 1))
        assertEquals(posMapOneLine(intArrayOf(1, -1, -1, -1)), newM)
    }

    private fun posMapOneLine(array: IntArray) =
        PositionMap(array + IntArray(boardArraySize - array.size) { -1 })

    private fun genPosMapForOneLine(array: IntArray): PositionMap {
        val m = posMapOneLine(array)
        val newM = PositionMap()
        val moves = mutableListOf<MoveGenerator.Move>()
        MoveGenerator.moveMapLine(
            listOf(0, 1, 2, 3),
            m,
            newM,
            moves,
        )
        return newM
    }
}
