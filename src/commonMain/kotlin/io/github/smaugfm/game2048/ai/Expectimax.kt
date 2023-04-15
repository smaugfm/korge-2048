package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.Direction.Companion.directions
import io.github.smaugfm.game2048.core.MoveBoardResult
import io.github.smaugfm.game2048.core.Tile
import io.github.smaugfm.game2048.maxAiDepth
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.benchmark.format
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class Expectimax<T : Board<T>>(
    private val heuristics: Heuristics<T>
) {

    private
    val dispatcher = Dispatchers.createFixedThreadDispatcher("ai", directions.size)

    private val moveBoardCounter = AtomicLong(0)
    private val Duration.mpm: String
        get() =
            ((moveBoardCounter.toDouble() / inWholeNanoseconds) * 1000000000000).format(0) + "m/s"

    suspend fun findBestMove(
        scope: CoroutineScope,
        board: T
    ): Deferred<MoveBoardResult<T>?> =
        scope.async {
            moveBoardCounter.set(0)
            measureTimedValue {
                findBestDirections(scope, board)
//            randomDirections(scope, board)
                    .map { board.moveBoardGenerateMoves(it) }
                    .firstOrNull { it.board != board }
            }.also {
                println("findBestMove: ${it.duration}, moves: ${moveBoardCounter.get()}, speed: ${it.duration.mpm} m/s")
            }.value
        }

    @Suppress("unused")
    private fun randomDirections(scope: CoroutineScope, board: T): List<Direction> =
        directions.toMutableList().also {
            it.shuffle()
        }

    private suspend fun findBestDirections(scope: CoroutineScope, board: T): List<Direction> =
        directions.map {
            scope.async(dispatcher) {
                it to score(board, it)
            }
        }
            .awaitAll()
            .sortedByDescending { it.second }
            .map { it.first }

    private fun score(board: T, it: Direction): Long {
        val newBoard = board.moveBoard(it)
        if (newBoard == board)
            return 0L

        return recursiveScore(newBoard, 0, maxAiDepth)
    }

    private fun recursiveScore(board: T, currentDepth: Int, maxDepth: Int): Long {
        if (currentDepth == maxDepth)
            return heuristics.evaluate(board)

        return board.iterateEveryEmptySpace { Tile.TWO }.zip(
            board.iterateEveryEmptySpace { Tile.FOUR }
        ).map { it.first.first to it.second.first }
            .map { (boardWithTwo, boardWithFour) ->
                val scoreBoard2 = calculateMoveScore(boardWithTwo, currentDepth, maxDepth)
                val scoreBoard4 = calculateMoveScore(boardWithFour, currentDepth, maxDepth)

                (scoreBoard2 / 10) * 9 + scoreBoard4 / 10
            }.sum()
    }

    private fun calculateMoveScore(board: T, currentDepth: Int, maxDepth: Int): Long =
        directions
            .map {
                val newBoard = board.moveBoard(it)
                moveBoardCounter.incrementAndGet()
                if (newBoard == board) return@map 0

                recursiveScore(newBoard, currentDepth + 1, maxDepth)
            }.max()

}
