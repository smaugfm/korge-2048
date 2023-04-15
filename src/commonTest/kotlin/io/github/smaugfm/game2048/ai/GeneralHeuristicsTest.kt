package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.ai.general.GeneralHeuristics
import io.github.smaugfm.game2048.core.general.GeneralBoard
import org.junit.Test
import kotlin.test.assertEquals

class GeneralHeuristicsTest {

    @Test
    fun monotonicityTest() {
        val maxScoreBoards = listOf(
            GeneralBoard(
                intArrayOf(
                    4, 3, 2, 1,
                    3, 3, 2, 1,
                    2, 2, 2, 1,
                    1, 1, 1, 1
                )
            ),
            GeneralBoard(
                intArrayOf(
                    1, 2, 3, 4,
                    1, 2, 3, 3,
                    1, 2, 2, 2,
                    1, 1, 1, 1
                )
            ),
            GeneralBoard(
                intArrayOf(
                    1, 1, 1, 1,
                    1, 2, 2, 2,
                    1, 2, 3, 3,
                    1, 2, 3, 4
                )
            ),
            GeneralBoard(
                intArrayOf(
                    1, 1, 1, 1,
                    2, 2, 2, 1,
                    3, 3, 2, 1,
                    4, 3, 2, 1
                )
            ),
        )

        val scores = maxScoreBoards.map {
            GeneralHeuristics.monotonicityHeuristic(it)
        }
        assertEquals(1, scores.toSet().size)
    }

    @Test
    fun test() {

        println(
            GeneralHeuristics.monotonicityHeuristic(
                GeneralBoard(
                    intArrayOf(
                        1, 1, 1, 1,
                        1, 2, 2, 2,
                        1, 2, 3, 3,
                        1, 2, 3, 4
                    )
                ),
            )
        )
        println(
            GeneralHeuristics.monotonicityHeuristic(
                GeneralBoard(
                    intArrayOf(
                        4, 3, 2, 1,
                        4, 3, 2, 1,
                        4, 3, 2, 1,
                        4, 3, 2, 1,
                    )
                )
            )
        )

        println(
            GeneralHeuristics.monotonicityHeuristic(
                GeneralBoard(
                    intArrayOf(
                        1, 1, 1, 1,
                        1, 2, 2, 1,
                        1, 2, 2, 1,
                        1, 1, 1, 1,
                    )
                )
            )
        )
    }
}
