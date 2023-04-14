package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board
import org.junit.Test
import kotlin.test.assertEquals

class BoardEvaluationTest {

    @Test
    fun monotonicityTest() {
        val maxScoreBoards = listOf(
            Board(
                intArrayOf(
                    4, 3, 2, 1,
                    3, 3, 2, 1,
                    2, 2, 2, 1,
                    1, 1, 1, 1
                )
            ),
            Board(
                intArrayOf(
                    1, 2, 3, 4,
                    1, 2, 3, 3,
                    1, 2, 2, 2,
                    1, 1, 1, 1
                )
            ),
            Board(
                intArrayOf(
                    1, 1, 1, 1,
                    1, 2, 2, 2,
                    1, 2, 3, 3,
                    1, 2, 3, 4
                )
            ),
            Board(
                intArrayOf(
                    1, 1, 1, 1,
                    2, 2, 2, 1,
                    3, 3, 2, 1,
                    4, 3, 2, 1
                )
            ),
        )

        val scores = maxScoreBoards.map {
            Heuristics.monotonicityHeuristic(it)
        }
        assertEquals(1, scores.toSet().size)
    }

    @Test
    fun test() {

        println(
            Heuristics.monotonicityHeuristic(
                Board(
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
            Heuristics.monotonicityHeuristic(
                Board(
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
            Heuristics.monotonicityHeuristic(
                Board(
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
