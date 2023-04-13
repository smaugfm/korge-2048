package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.ui.UiBlock.Companion.toPosition
import kotlin.random.Random

object MoveGenerator {

    private val indices = 0 until boardArraySize
    private val directionIndexesMap = mapOf(
        Direction.TOP to
            (0 until boardSize).associateWith {
                (it until boardArraySize step boardSize).toList()
            },
        Direction.BOTTOM to
            (0 until boardSize).associateWith {
                (it until boardArraySize step boardSize).reversed().toList()
            },
        Direction.LEFT to
            (0 until boardSize).associateWith {
                (it * boardSize until ((it + 1) * boardSize)).toList()
            },
        Direction.RIGHT to
            (0 until boardSize).associateWith {
                (it * boardSize until ((it + 1) * boardSize)).reversed().toList()
            },
    )

    sealed interface BoardMove {
        data class Move(val from: Int, val to: Int) : BoardMove
        data class Merge(val from1: Int, val from2: Int, val to: Int) : BoardMove
    }

    data class MoveBoardResult(
        val board: Board,
        val moves: List<BoardMove>,
    )

    data class RandomBlockResult(
        val power: PowerOfTwo,
        val index: Int
    )

    fun placeRandomBlock(board: Board): RandomBlockResult? {
        val index = board.getRandomFreeIndex() ?: return null
        val power = if (Random.nextDouble() < 0.9) PowerOfTwo(1) else PowerOfTwo(2)
        board[index] = power.power

        return RandomBlockResult(power, index)
    }

    fun moveBoard(board: Board, direction: Direction): MoveBoardResult {
        val newBoard = Board()
        val moves = mutableListOf<BoardMove>()

        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap.getValue(direction).getValue(i)
            moveLine(indexes, board, newBoard, moves)
        }

        return MoveBoardResult(newBoard, moves)
    }

    private fun firstNotEmpty(board: Board, indexes: List<Int>, startFrom: Int? = -1): Int? {
        if (startFrom == null)
            return null
        var i = startFrom + 1
        while (i < indexes.size) {
            if (board[indexes[i]] > 0)
                return i
            i++
        }

        return null
    }

    fun moveLine(
        indexes: List<Int>,
        board: Board,
        newBoard: Board,
        moves: MutableList<BoardMove>
    ) {
        var oldCursor1: Int? = firstNotEmpty(board, indexes) ?: return
        var oldCursor2 = firstNotEmpty(board, indexes, oldCursor1)
        var newCursor = 0

        while (oldCursor1 != null) {
            if (oldCursor2 != null && board[indexes[oldCursor1]] == board[indexes[oldCursor2]]) {
                newBoard[indexes[newCursor]] = board[indexes[oldCursor1]] + 1
                moves.add(
                    BoardMove.Merge(
                        indexes[oldCursor1],
                        indexes[oldCursor2],
                        indexes[newCursor]
                    )
                )
                oldCursor1 = firstNotEmpty(board, indexes, oldCursor2)
                oldCursor2 = firstNotEmpty(board, indexes, oldCursor1)
                newCursor++
            } else {
                newBoard[indexes[newCursor]] = board[indexes[oldCursor1]]
                if (oldCursor1 != newCursor)
                    moves.add(BoardMove.Move(indexes[oldCursor1], indexes[newCursor]))
                newCursor++
                if (oldCursor2 != null) {
                    oldCursor1 = oldCursor2
                    oldCursor2 = firstNotEmpty(board, indexes, oldCursor2)
                } else {
                    return
                }
            }
        }
    }


    fun hasAvailableMoves(board: Board): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(board, i)
        }

    private fun Board.getXY(x: Int, y: Int): Int {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return -1
        return this[index]
    }

    private fun hasAdjacentEqualPosition(board: Board, i: Int): Boolean {
        val value = board[i]
        val pos = i.toPosition()
        return value == board.getXY(pos.x - 1, pos.y) ||
            value == board.getXY(pos.x + 1, pos.y) ||
            value == board.getXY(pos.x, pos.y - 1) ||
            value == board.getXY(pos.x, pos.y + 1)
    }
}
