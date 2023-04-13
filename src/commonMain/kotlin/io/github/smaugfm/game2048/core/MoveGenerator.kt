package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import korlibs.datastructure.toIntList
import kotlin.random.Random

object MoveGenerator {
    private val indices = 0 until boardArraySize
    private val directionIndexesMap: Array<Array<IntArray>> =
        initDirectionIndexesMap()

    sealed interface BoardMove {
        data class Move(val from: Int, val to: Int) : BoardMove
        data class Merge(val from1: Int, val from2: Int, val to: Int) : BoardMove
    }

    data class MoveBoardResult(
        val board: Board,
        val moves: List<BoardMove>,
    )

    data class RandomBlockResult(
        val power: Tile,
        val index: TileIndex
    )

    fun hasAvailableMoves(board: Board): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(board, i)
        }

    fun placeRandomBlock(board: Board): RandomBlockResult? {
        val index = board.getRandomFreeIndex() ?: return null
        val power = if (Random.nextDouble() < 0.9) Tile(1) else Tile(2)
        board[index] = power

        return RandomBlockResult(power, index)
    }

    fun moveBoard(
        board: Board,
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int) -> Unit,
    ): Board {
        val newBoard = Board()

        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction.ordinal][i]
            moveLine(indexes, board, newBoard, addMove, addMerge)
        }

        return newBoard
    }

    fun moveBoard(
        board: Board,
        direction: Direction
    ): MoveBoardResult {
        val moves = mutableListOf<BoardMove>()

        val newBoard = moveBoard(
            board,
            direction,
            { from, to -> moves.add(BoardMove.Move(from, to)) },
            { from1, from2, to -> moves.add(BoardMove.Merge(from1, from2, to)) }
        )

        return MoveBoardResult(newBoard, moves)
    }

    fun moveLine(
        indexes: IntArray,
        board: Board,
        newBoard: Board,
        moves: MutableList<BoardMove>
    ) {
        moveLine(indexes, board, newBoard, { from, to ->
            moves.add(BoardMove.Move(from, to))
        },
            { from1, from2, to ->
                moves.add(BoardMove.Merge(from1, from2, to))
            }
        )
    }

    private fun moveLine(
        indexes: IntArray,
        board: Board,
        newBoard: Board,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int) -> Unit,
    ) {
        var cur1: Int? = firstNotEmpty(board, indexes) ?: return
        var cur2 = firstNotEmpty(board, indexes, cur1)
        var newCur = 0

        while (cur1 != null) {
            if (cur2 != null && board[indexes[cur1]] == board[indexes[cur2]]) {
                newBoard[indexes[newCur]] = board[indexes[cur1]].next()
                addMerge(
                    indexes[cur1],
                    indexes[cur2],
                    indexes[newCur]
                )
                cur1 = firstNotEmpty(board, indexes, cur2)
                cur2 = firstNotEmpty(board, indexes, cur1)
                newCur++
            } else {
                newBoard[indexes[newCur]] = board[indexes[cur1]]
                if (cur1 != newCur)
                    addMove(
                        indexes[cur1],
                        indexes[newCur]
                    )
                newCur++
                if (cur2 != null) {
                    cur1 = cur2
                    cur2 = firstNotEmpty(board, indexes, cur2)
                } else {
                    return
                }
            }
        }
    }

    private fun firstNotEmpty(
        board: Board,
        indexes: IntArray,
        startFrom: Int? = -1
    ): Int? {
        if (startFrom == null)
            return null
        var i = startFrom + 1
        while (i < indexes.size) {
            if (board[indexes[i]].isNotEmpty)
                return i
            i++
        }

        return null
    }

    private fun Board.getXY(x: Int, y: Int): Tile {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return Tile.EMPTY
        return this[index]
    }

    private fun hasAdjacentEqualPosition(board: Board, i: TileIndex): Boolean {
        val value = board[i]
        val x = i % boardSize
        val y = i / boardSize
        return value == board.getXY(x - 1, y) ||
            value == board.getXY(x + 1, y) ||
            value == board.getXY(x, y - 1) ||
            value == board.getXY(x, y + 1)
    }

    private fun initDirectionIndexesMap(): Array<Array<IntArray>> =
        Direction.values().map { dir ->
            (0 until boardSize).map {
                when (dir) {
                    Direction.TOP ->
                        it until boardArraySize step boardSize

                    Direction.BOTTOM ->
                        (it until boardArraySize step boardSize).reversed()

                    Direction.LEFT ->
                        it * boardSize until ((it + 1) * boardSize)

                    Direction.RIGHT ->
                        (it * boardSize until ((it + 1) * boardSize)).reversed()
                }
                    .toList()
                    .toIntList()
                    .toIntArray()
            }.toTypedArray()
        }.toTypedArray()

}
