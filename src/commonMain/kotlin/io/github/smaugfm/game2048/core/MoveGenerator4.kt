package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.core.MoveGenerator.Companion.directions
import korlibs.datastructure.random.FastRandom

object MoveGenerator4 : MoveGenerator<Board4> {

    override fun hasAvailableMoves(board: Board4): Boolean =
        directions.any {
            moveBoard(board, it).board != board
        }

    override fun placeRandomBlock(board: Board4): RandomBlockResult<Board4>? {
        val emptyTiles = board.countEmptyTiles()
        if (emptyTiles == 0)
            return null

        val emptyIndex = FastRandom.Default.nextInt(emptyTiles)
        val tile = (if (FastRandom.nextDouble() < 0.9) Tile.TWO else Tile.FOUR)
        var tileUL = tile.power.toULong()
        var temp = board.packed

        var i = 0
        while (true) {
            while (temp and 0xFUL != 0UL) {
                temp = temp shr 4
                tileUL = tileUL shl 4
            }
            if (i >= emptyIndex)
                break
            temp = temp shr 4
            tileUL = tileUL shl 4
            i++
        }

        return RandomBlockResult(Board4(board.packed or tileUL), tile, i)
    }

    override fun moveBoard(
        board: Board4,
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit
    ): Board4 {
        TODO("Not yet implemented")
    }
}
