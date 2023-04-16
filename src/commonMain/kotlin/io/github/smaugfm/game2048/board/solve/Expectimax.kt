package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Board
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.MoveBoardResult
import io.github.smaugfm.game2048.maxAiDepth
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
abstract class Expectimax<T : Board<T>>(
    protected val heuristics: Heuristics<T>,
    private val log: Boolean = true
) {
    private var moveBoardCounter = 0L

    fun findBestMove(board: T): MoveBoardResult<T>? {
        moveBoardCounter = 0
        return measureTimedValue {
            findBestDirection(board)
                .map { board.moveGenerateMoves(it) }
                .firstOrNull { it.board != board }
        }.also {
            if (log)
                println(
                    "findBestMove: ${it.duration}, " +
                        "moves: ${moveBoardCounter}, " +
                        "speed: ${it.duration.mpm(moveBoardCounter)} m/s"
                )
        }.value
    }

    private fun findBestDirection(
        board: T
    ): List<Direction> =
        directions.map { it to topLevelNode(board, it) }
            .sortedByDescending { it.second.first }
            .also { results ->
                if (log) {
                    println("moves:\n")
                    results.forEach {
                        println("${it.first} ${it.second.first.roundToLong()}")
                        println(it.second.second)
                    }
                }
            }
            .map { it.first }

    private fun topLevelNode(
        board: T,
        it: Direction
    ): Pair<Double, T> {
        val newBoard = board.move(it)
        if (newBoard == board)
            return Pair(Double.NEGATIVE_INFINITY, board)

        return expectimaxNode(newBoard, 0, 1.0, maxAiDepth) to newBoard
    }

    private fun expectimaxNode(
        board: T,
        depth: Int,
        prob: Double,
        maxDepth: Int
    ): Double {
        if (prob < PROBABILITY_THRESHOLD || depth >= maxDepth)
            return heuristics.evaluate(board)

        val emptyCount = board.countEmptyTiles()
        val emptyTileProb = prob / emptyCount

        return emptyTilesScoresSum(board, emptyTileProb, depth, maxDepth) / emptyCount
    }

    protected abstract fun emptyTilesScoresSum(
        board: T,
        emptyTileProb: Double,
        depth: Int,
        maxDepth: Int
    ): Double

    protected fun moveNode(
        board: T,
        prob: Double,
        depth: Int,
        maxDepth: Int
    ): Double {
        return directions
            .map {
                val newBoard = board.move(it)
                moveBoardCounter++

                if (newBoard == board)
                    return@map Double.NEGATIVE_INFINITY
                expectimaxNode(newBoard, depth + 1, prob, maxDepth)
            }.max()
    }

    companion object {
        const val PROBABILITY_THRESHOLD = 0.0001

        private fun Duration.mpm(moves: Long): String {
            return ((moves.toDouble() / inWholeMilliseconds) * 1000)
                .roundToLong().toString() + "m/s"
        }
    }
}
