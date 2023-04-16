package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Board
import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.board.MoveBoardResult
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.maxAiDepth
import korlibs.io.concurrent.atomic.KorAtomicLong
import korlibs.io.concurrent.atomic.incrementAndGet
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.*
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class Expectimax<T : Board<T>>(
    private val heuristics: Heuristics<T>
) {
    private val dispatcher =
        Dispatchers.createFixedThreadDispatcher("ai", directions.size)

    private val moveBoardCounter = KorAtomicLong(0)

    suspend fun findBestMove(
        scope: CoroutineScope,
        board: T
    ): Deferred<MoveBoardResult<T>?> =
        scope.async {
            moveBoardCounter.value = 0
            measureTimedValue {
                findBestDirection(scope, board)
                    .map { board.moveGenerateMoves(it) }
                    .firstOrNull { it.board != board }
            }.also {
                println(
                    "findBestMove: ${it.duration}, " +
                        "moves: ${moveBoardCounter.value}, " +
                        "speed: ${it.duration.mpm(moveBoardCounter.value)} m/s"
                )
            }.value
        }

    private suspend fun findBestDirection(
        scope: CoroutineScope,
        board: T
    ): List<Direction> =
        directions.map {
            scope.async(dispatcher) {
                it to topLevelNode(board, it)
            }
        }
            .awaitAll()
            .sortedByDescending { it.second.first }
            .also { results ->
                println("moves:\n")
                results.forEach {
                    println("${it.first} ${it.second.first.roundToLong()}")
                    println(it.second.second)
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

        return board.placeEveryEmpty(emptyCount) { Tile.TWO }.zip(
            board.placeEveryEmpty(emptyCount) { Tile.FOUR }
        ).map { it.first.newBoard to it.second.newBoard }
            .map { (boardWithTwo, boardWithFour) ->
                val scoreBoard2 =
                    moveNode(boardWithTwo, depth, emptyTileProb * 0.9, maxDepth) * 0.9
                val scoreBoard4 =
                    moveNode(boardWithFour, depth, emptyTileProb * 0.1, maxDepth) * 0.1

                scoreBoard2 + scoreBoard4
            }.sum() / emptyCount
    }

    private fun moveNode(
        board: T,
        depth: Int,
        prob: Double,
        maxDepth: Int
    ): Double {
        return directions
            .map {
                val newBoard = board.move(it)
                moveBoardCounter.incrementAndGet()

                if (newBoard == board)
                    return@map Double.NEGATIVE_INFINITY
                expectimaxNode(newBoard, depth + 1, prob, maxDepth)
            }.max()
    }

    companion object {
        private const val PROBABILITY_THRESHOLD = 0.0001

        private fun Duration.mpm(moves: Long): String {
            return ((moves.toDouble() / inWholeMilliseconds) * 1000)
                .roundToLong().toString() + "m/s"
        }
    }
}
