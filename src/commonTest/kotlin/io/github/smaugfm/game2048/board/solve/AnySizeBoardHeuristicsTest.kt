package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.expectimax.impl.AnySizeExpectimax
import io.github.smaugfm.game2048.heuristics.impl.AnySizeBoardHeuristics
import korlibs.datastructure.IntArray2
import kotlin.math.roundToLong
import kotlin.test.Test

class AnySizeBoardHeuristicsTest {
    private val heuristics = AnySizeBoardHeuristics()
//private val heuristics = SleepyCoderAnySizeHeuristic()
//    private val heuristics = AzakyAnySizeHeuristics()

    private val expectimax = AnySizeExpectimax(heuristics)

    @Test
    fun t() {
        val b = AnySizeBoard.fromArray(
            intArrayOf(
                9, 6, 1, 1,
                3, 5, 2, 0,
                4, 4, 4, 1,
                2, 1, 8, 0,
            )
        )
        val right = b.move(Direction.RIGHT)
        val rightScore = heuristics.evaluate(right)
        expectimax.findBestDirection(b)
        println("Right score: $rightScore")
    }

    @Test
    fun evaluateTest() {
        show(
            intArrayOf(
                4, 5, 3, 0,
                4, 0, 0, 0,
                8, 1, 0, 0,
                9, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                5, 5, 3, 0,
                8, 1, 0, 0,
                9, 2, 0, 0,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 0, 0, 0,
                5, 5, 0, 0,
                8, 1, 0, 0,
                9, 2, 3, 0,
            )
        )
        show(
            intArrayOf(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                1, 2, 3, 4,
                1, 2, 3, 3,
                1, 2, 2, 2,
                1, 1, 1, 1,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 4,
                0, 2, 3, 3,
                0, 2, 2, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 10,
                0, 2, 3, 3,
                0, 2, 2, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 3,
                0, 2, 10, 3,
                0, 2, 2, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 4,
                0, 2, 10, 3,
                0, 2, 10, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 10,
                0, 2, 4, 10,
                0, 2, 3, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 0, 1,
                0, 2, 5, 10,
                0, 2, 3, 9,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 0, 10,
                0, 2, 5, 9,
                0, 2, 3, 1,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 0, 9,
                0, 2, 5, 10,
                0, 2, 3, 1,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 0, 9,
                0, 2, 5, 1,
                0, 2, 3, 10,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 0, 9,
                0, 2, 5, 1,
                0, 2, 3, 0,
                0, 0, 0, 10,
            )
        )
    }

    @Test
    fun investigate1() {
        show(
            intArrayOf(
                0, 2, 3, 4,
                0, 2, 10, 3,
                0, 2, 10, 2,
                0, 0, 0, 0,
            )
        )
        show(
            intArrayOf(
                0, 2, 3, 10,
                0, 2, 4, 10,
                0, 2, 3, 2,
                0, 0, 0, 0,
            )
        )

        val fr1 = showLine(intArrayOf(0, 2, 3, 4))
        val fr2 = showLine(intArrayOf(0, 2, 10, 3))
        val fr3 = showLine(intArrayOf(0, 2, 10, 2))
        val fr4 = showLine(intArrayOf(0, 0, 0, 0))
        println("first sum rows: ${(fr1 + fr2 + fr3 + fr4).roundToLong()}")

        val sr1 = showLine(intArrayOf(0, 2, 3, 10))
        val sr2 = showLine(intArrayOf(0, 2, 4, 10))
        val sr3 = showLine(intArrayOf(0, 2, 3, 2))
        val sr4 = showLine(intArrayOf(0, 0, 0, 0))
        println("second sum rows: ${(sr1 + sr2 + sr3 + sr4).roundToLong()}")

        val fc1 = showLine(intArrayOf(0, 0, 0, 0))
        val fc2 = showLine(intArrayOf(2, 2, 2, 0))
        val fc3 = showLine(intArrayOf(3, 10, 10, 0))
        val fc4 = showLine(intArrayOf(4, 3, 2, 0))
        println("first sum cols: ${(fc1 + fc2 + fc3 + fc4).roundToLong()}")

        val sc1 = showLine(intArrayOf(0, 0, 0, 0))
        val sc2 = showLine(intArrayOf(2, 2, 2, 0))
        val sc3 = showLine(intArrayOf(3, 4, 3, 0))
        val sc4 = showLine(intArrayOf(10, 10, 2, 0))
        println("second sum cols: ${(sc1 + sc2 + sc3 + sc4).roundToLong()}")

        println("first: ${(fr1 + fr2 + fr3 + fr4 + fc1 + fc2 + fc3 + fc4).roundToLong()}")
        println("second: ${(sr1 + sr2 + sr3 + sr4 + sc1 + sc2 + sc3 + sc4).roundToLong()}")
    }

    @Test
    fun investigate2() {
        val fr2 = showLine(intArrayOf(0, 2, 10, 3))
        val sr1 = showLine(intArrayOf(0, 2, 3, 10))
    }

    @Test
    fun evaluateLineTest() {
        showLine(intArrayOf(0, 0, 0, 0))

        showLine(intArrayOf(1, 0, 0, 0))
        showLine(intArrayOf(0, 1, 0, 0))
        showLine(intArrayOf(0, 0, 1, 0))
        showLine(intArrayOf(0, 0, 0, 1))

        showLine(intArrayOf(1, 1, 0, 0))
        showLine(intArrayOf(0, 1, 1, 0))
        showLine(intArrayOf(0, 0, 1, 1))

        showLine(intArrayOf(1, 1, 1, 0))
        showLine(intArrayOf(0, 1, 1, 1))

        showLine(intArrayOf(1, 1, 1, 1))
        showLine(intArrayOf(2, 2, 2, 2))
        showLine(intArrayOf(3, 3, 3, 3))

        showLine(intArrayOf(0, 1, 2, 3))
        showLine(intArrayOf(1, 2, 3, 0))
        showLine(intArrayOf(0, 2, 3, 0))
        showLine(intArrayOf(0, 2, 3, 4))
        showLine(intArrayOf(0, 2, 3, 4))
        showLine(intArrayOf(0, 2, 6, 0))
        showLine(intArrayOf(0, 0, 6, 0))
        showLine(intArrayOf(0, 5, 6, 0))
        showLine(intArrayOf(0, 0, 5, 6))
        showLine(intArrayOf(6, 5, 5, 6))
        showLine(intArrayOf(6, 2, 2, 6))
        showLine(intArrayOf(1, 2, 3, 4))
    }

    private fun show(it: IntArray): Float {
        val e = heuristics.evaluate(
            AnySizeBoard.fromArray(it)
        )
        println(
            "${IntArray2(4, 4, it)} ${e.roundToLong()} \n"
        )
        return e
    }

    private fun showLine(a: IntArray): Float {
        val e = heuristics.evaluateLine(
            AnySizeBoard.fromArray(a),
            intArrayOf(0, 1, 2, 3)
        )
        println("${a.contentToString()} - ${e.roundToLong()}")
        return e
    }
}
