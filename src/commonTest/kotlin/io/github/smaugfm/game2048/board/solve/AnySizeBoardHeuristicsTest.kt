package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.AnySizeBoard
import org.junit.Test
import kotlin.math.roundToInt

class AnySizeBoardHeuristicsTest {
    private val heuristics = AnySizeBoardHeuristics()

    @Test
    fun evaluateTest() {
        show(intArrayOf(0, 0, 0, 0))

        show(intArrayOf(1, 0, 0, 0))
        show(intArrayOf(0, 1, 0, 0))
        show(intArrayOf(0, 0, 1, 0))
        show(intArrayOf(0, 0, 0, 1))

        show(intArrayOf(1, 1, 0, 0))
        show(intArrayOf(0, 1, 1, 0))
        show(intArrayOf(0, 0, 1, 1))

        show(intArrayOf(1, 1, 1, 0))
        show(intArrayOf(0, 1, 1, 1))

        show(intArrayOf(1, 1, 1, 1))
        show(intArrayOf(2, 2, 2, 2))
        show(intArrayOf(3, 3, 3, 3))

        show(intArrayOf(0, 1, 2, 3))
        show(intArrayOf(1, 2, 3, 0))
        show(intArrayOf(0, 2, 3, 0))
        show(intArrayOf(0, 2, 3, 4))
        show(intArrayOf(0, 2, 3, 4))
        show(intArrayOf(0, 2, 6, 0))
        show(intArrayOf(0, 0, 6, 0))
        show(intArrayOf(0, 5, 6, 0))
        show(intArrayOf(0, 0, 5, 6))
        show(intArrayOf(6, 5, 5, 6))
        show(intArrayOf(6, 2, 2, 6))
        show(intArrayOf(1, 2, 3, 4))
    }

    private fun show(it: IntArray) {
        println(
            "${it.contentToString()} - " +
                heuristics.evaluateLine(
                    AnySizeBoard(it),
                    intArrayOf(0, 1, 2, 3)
                ).roundToInt()
        )
    }
}
