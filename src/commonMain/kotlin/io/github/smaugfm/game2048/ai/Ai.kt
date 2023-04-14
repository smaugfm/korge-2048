package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveGenerator
import io.github.smaugfm.game2048.core.Tile
import io.github.smaugfm.game2048.maxAiDepth
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.benchmark.format
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
object Ai {
    private val emptyAddMove = { _: Int, _: Int -> }
    private val emptyAddMerge = { _: Int, _: Int, _: Int, _: Tile -> }
    private val boardIndexes = 0 until boardArraySize
    private val directions = Direction.values()
    private val dispatcher = Dispatchers.createFixedThreadDispatcher("ai", directions.size)

    private val moveBoardCounter = AtomicLong(0)
    private val Duration.mpm: String
        get() =
            ((moveBoardCounter.toDouble() / inWholeNanoseconds) * 1000000000000).format(0) + "m/s"

    suspend fun findBestMove(scope: CoroutineScope, board: Board): Deferred<MoveGenerator.MoveBoardResult?> =
        scope.async {
            moveBoardCounter.set(0)
            measureTimedValue {
                findBestDirections(scope, board)
//            randomDirections(scope, board)
                    .map { MoveGenerator.moveBoard(board, it) }
                    .firstOrNull { it.board != board }
            }.also {
                println("findBestMove: ${it.duration}, moves: ${moveBoardCounter.get()}, speed: ${it.duration.mpm} m/s")
            }.value
        }

    private fun randomDirections(scope: CoroutineScope, board: Board): List<Direction> =
        directions.toMutableList().also {
            it.shuffle()
        }

    private suspend fun findBestDirections(scope: CoroutineScope, board: Board): List<Direction> =
        directions.map {
            scope.async(dispatcher) {
                it to score(board, it)
            }
        }
            .awaitAll()
            .sortedByDescending { it.second }
            .map { it.first }

    private fun score(board: Board, it: Direction): Long {
        val newBoard = MoveGenerator.moveBoard(board, it, emptyAddMove, emptyAddMerge)
        if (newBoard == board)
            return 0L

        return recursiveScore(newBoard, 0, maxAiDepth)
    }

    private fun recursiveScore(board: Board, currentDepth: Int, maxDepth: Int): Long {
        if (currentDepth == maxDepth)
            return Heuristics.evaluate(board)

        var totalScore = 0L
        for (i in boardIndexes) {
            if (board[i].isNotEmpty)
                continue

            val newBoard2 = Board(board)
            newBoard2[i] = Tile.TWO
            val scoreBoard2 = calculateMoveScore(newBoard2, currentDepth, maxDepth)
            totalScore += (scoreBoard2 / 10) * 9

            val newBoard4 = Board(board)
            newBoard2[i] = Tile.FOUR
            val scoreBoard4 = calculateMoveScore(newBoard4, currentDepth, maxDepth)
            totalScore += scoreBoard4 / 10
        }

        return totalScore
    }

    private fun calculateMoveScore(board: Board, currentDepth: Int, maxDepth: Int): Long =
        Direction
            .values()
            .map {
                val newBoard = MoveGenerator.moveBoard(board, it, emptyAddMove, emptyAddMerge)
                moveBoardCounter.incrementAndGet()
                if (newBoard == board) return@map 0

                recursiveScore(newBoard, currentDepth + 1, maxDepth)
            }.max()

}
