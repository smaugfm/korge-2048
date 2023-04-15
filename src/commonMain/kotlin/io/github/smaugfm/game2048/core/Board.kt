package io.github.smaugfm.game2048.core

typealias TileIndex = Int

interface Board<out T : Board<T>> {
    fun hasAvailableMoves(): Boolean
    fun moveBoard(direction: Direction): T
    fun moveBoardGenerateMoves(direction: Direction): MoveBoardResult<T>
    fun placeRandomBlock(): RandomBlockResult<T>?

    fun countEmptyTiles(): Int
    fun iterateEveryEmptySpace(
        emptyTilesCount: Int = this.countEmptyTiles(),
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<Pair<T, TileIndex>>
}
