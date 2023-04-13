package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import korlibs.datastructure.IntArray2

typealias TileIndex = Int

class Board(
    private val array: IntArray = IntArray(boardArraySize) { Tile.EMPTY.power }
) {
    init {
        require(array.size == boardArraySize)
    }

    operator fun get(index: TileIndex) = Tile(array[index])

    operator fun set(x: TileIndex, value: Tile) {
        array[x] = value.power
    }

    fun getRandomFreeIndex(): Int? =
        array.withIndex()
            .filter { Tile(it.value).isEmpty }
            .randomOrNull()?.index

    fun powers() = array.map(::Tile).toTypedArray()

    override fun equals(other: Any?): Boolean =
        (other is Board) && this.array.contentEquals(other.array)

    override fun hashCode() = array.hashCode()

    override fun toString() =
        IntArray2(boardSize, boardSize, array).toString()
}
