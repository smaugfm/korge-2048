package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.ui.UiBlock.Companion.toPosition

object MoveGenerator {

    private val indices = 0 until boardArraySize
    private val directionIndexesMap = mapOf(
        Direction.TOP to { i: Int -> i until boardArraySize step boardSize },
        Direction.BOTTOM to { i: Int -> (i until boardArraySize step boardSize).reversed() },
        Direction.LEFT to { i: Int -> i * boardSize until ((i + 1) * boardSize) },
        Direction.RIGHT to { i: Int -> (i * boardSize until ((i + 1) * boardSize)).reversed() },
    )

    data class Move(
        val from: Int, val to: Int, val merge: Boolean
    )

    data class MoveBoardResult(
        val board: Board,
        val moves: List<Move>,
    )

    fun moveBoard(board: Board, direction: Direction): MoveBoardResult {
        val newMap = Board()
        val moves = mutableListOf<Move>()
        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction]!!(i)
            moveMapLine(indexes, board, newMap, moves)
        }

        return MoveBoardResult(newMap, moves)
    }

    fun moveMapLine(
        indexes: Iterable<Int>,
        board: Board,
        newMap: Board,
        moves: MutableList<Move>,
    ) {
        val newIndexes = indexes.iterator()
        var newMapIndex = newIndexes.next()
        var merged = false

        for (oldMapIndex in indexes) {
            val moving = board[oldMapIndex]
            if (moving <= 0)
                continue
            if (!merged && moving == newMap[newMapIndex]) {
                newMap[newMapIndex] += 1
                merged = true
            } else {
                if (newMap[newMapIndex] > 0)
                    newMapIndex = newIndexes.next()
                newMap[newMapIndex] = moving
                merged = false
            }
            if (oldMapIndex != newMapIndex) {
                moves.add(Move(oldMapIndex, newMapIndex, merged))
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
