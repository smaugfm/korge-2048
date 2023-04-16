package io.github.smaugfm.game2048.board

typealias TileIndex = Int

interface Board<out T : Board<T>> {

    fun hasAvailableMoves(): Boolean
    fun move(direction: Direction): T
    fun moveGenerateMoves(direction: Direction): MoveBoardResult<T>
    fun placeRandomBlock(): TilePlacementResult<T>?

    fun countEmptyTiles(): Int
    fun placeEveryEmpty(
        emptyTilesCount: Int = this.countEmptyTiles(),
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<TilePlacementResult<T>>
}
