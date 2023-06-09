package io.github.smaugfm.game2048.board

const val boardSize = 4
const val boardArraySize = boardSize * boardSize

interface Board<out T : Board<T>> {

    fun transpose(): T
    fun hasAvailableMoves(): Boolean
    fun move(direction: Direction): T
    fun moveGenerateMoves(direction: Direction): MoveBoardResult<T>
    fun placeRandomTile(): TilePlacementResult<T>?
    fun placeTile(tile: Tile, i: TileIndex): T

    fun tiles(): Array<Tile>

    fun countEmptyTiles(): Int
}
