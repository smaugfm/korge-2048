package io.github.smaugfm.game2048.board

interface BoardFactory<T: Board<T>> {
    fun createEmpty(): T
    fun fromTiles(tiles: Array<Tile>): T
    fun fromArray(tiles: IntArray): T
}
