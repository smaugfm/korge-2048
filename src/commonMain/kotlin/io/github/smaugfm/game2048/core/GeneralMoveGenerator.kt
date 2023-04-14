package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import korlibs.datastructure.random.FastRandom
import korlibs.datastructure.toIntList

internal object GeneralMoveGenerator : MoveGenerator<GeneralBoard> {
    private val indices = 0 until boardArraySize

    private val directionIndexesMap: Array<Array<IntArray>> =
        initDirectionIndexesMap()

    override fun hasAvailableMoves(board: GeneralBoard): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(board, i)
        }

    override fun placeRandomBlock(board: GeneralBoard): RandomBlockResult<GeneralBoard>? {
        val index = board.powers()
            .withIndex()
            .filter { it.value.isEmpty }
            .randomOrNull()?.index ?: return null

        val tile = if (FastRandom.nextDouble() < 0.9) Tile.TWO else Tile.FOUR
        board[index] = tile

        return RandomBlockResult(board, tile, index)
    }

    override fun moveBoard(
        board: GeneralBoard,
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ): GeneralBoard {
        val newBoard = GeneralBoard()

        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction.ordinal][i]
            moveLine(indexes, board, newBoard, addMove, addMerge)
        }

        return newBoard
    }

    fun moveLine(
        indexes: IntArray,
        board: GeneralBoard,
        newBoard: GeneralBoard,
        moves: MutableList<BoardMove>
    ) {
        moveLine(indexes, board, newBoard, { from, to ->
            moves.add(BoardMove.Move(from, to))
        },
            { from1, from2, to, newTile ->
                moves.add(BoardMove.Merge(from1, from2, to, newTile))
            }
        )
    }

    private fun moveLine(
        indexes: IntArray,
        board: GeneralBoard,
        newBoard: GeneralBoard,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
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
                    indexes[newCur],
                    newBoard[indexes[newCur]]
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
        board: GeneralBoard,
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

    private fun GeneralBoard.getXY(x: Int, y: Int): Tile {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return Tile.EMPTY
        return this[index]
    }

    private fun hasAdjacentEqualPosition(board: GeneralBoard, i: TileIndex): Boolean {
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
