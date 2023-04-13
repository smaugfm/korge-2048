package io.github.smaugfm.game2048.core

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import korlibs.datastructure.IntArray2

class Board(
    private val array: IntArray = IntArray(boardArraySize) { -1 }
) {
    init {
        require(array.size == boardArraySize)
    }

    operator fun get(index: Int) = array[index]

    fun power(index: Int) = PowerOfTwo(get(index))

    operator fun set(x: Int, value: Int) {
        array[x] = value
    }

    fun getRandomFreeIndex(): Int? =
        array.withIndex().filter { it.value < 0 }.randomOrNull()?.index

    fun powers() = array

    override fun equals(other: Any?): Boolean =
        (other is Board) && this.array.contentEquals(other.array)

    override fun hashCode() = array.hashCode()

    override fun toString() =
        IntArray2(boardSize, boardSize, array).toString()
}
