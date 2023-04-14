package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import korlibs.datastructure.IntArray2

typealias TileIndex = Int

class Board(
    val array: IntArray = IntArray(boardArraySize) { Tile.EMPTY.power }
) {
    constructor(other: Board) : this(other.array.copyOf())

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun get(index: TileIndex) = Tile(array[index])
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun get(x: Int, y: Int) = Tile(array[y * boardSize + x])
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun set(x: TileIndex, value: Tile) {
        array[x] = value.power
    }

    fun powers() = array.map(::Tile).toTypedArray()

    override fun equals(other: Any?): Boolean =
        (other is Board) && this.array.contentEquals(other.array)

    override fun hashCode() = array.hashCode()

    override fun toString() =
        IntArray2(boardSize, boardSize, array).toString()
}
