package io.github.smaugfm.game2048.board

typealias TileIndex = Int

interface Board<out T : Board<T>> {

    fun hasAvailableMoves(): Boolean
    fun move(direction: Direction): T
    fun moveGenerateMoves(direction: Direction): MoveBoardResult<T>
    fun placeRandomTile(): TilePlacementResult<T>?
    fun placeTile(tile: Tile, i: TileIndex): T

    fun tiles(): Array<Tile>

    fun countEmptyTiles(): Int
}
