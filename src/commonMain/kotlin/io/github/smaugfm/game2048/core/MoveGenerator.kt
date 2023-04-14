package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize

interface MoveGenerator<T : Board> {
    fun hasAvailableMoves(board: T): Boolean

    fun moveBoard(
        board: T,
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ): T

    fun placeRandomBlock(board: T): RandomBlockResult<T>?

    fun moveBoard(
        board: T,
        direction: Direction
    ): MoveBoardResult<T> {
        val moves = mutableListOf<BoardMove>()

        val newBoard = moveBoard(
            board,
            direction,
            { from, to -> moves.add(BoardMove.Move(from, to)) },
            { from1, from2, to, newTile -> moves.add(BoardMove.Merge(from1, from2, to, newTile)) }
        )

        return MoveBoardResult<T>(newBoard, moves)
    }


    companion object : MoveGenerator<GeneralBoard> by GeneralMoveGenerator {
        val directions = Direction.values().toList().toTypedArray()
        val boardIndexes = 0 until boardArraySize
        val emptyAddMove = { _: Int, _: Int -> }
        val emptyAddMerge = { _: Int, _: Int, _: Int, _: Tile -> }
    }
}
