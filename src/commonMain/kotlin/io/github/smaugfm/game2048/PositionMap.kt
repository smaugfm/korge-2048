package io.github.smaugfm.game2048

import korlibs.datastructure.*
import kotlin.random.*

class PositionMap private constructor(
    private val array: IntArray2 =
        IntArray2(4, 4, -1)
): Iterable<Int> by array {
    fun calculateNewMap(
        direction: Direction,
        moves: MutableList<Pair<Int, Position>>,
        merges: MutableList<Triple<Int, Int, Position>>
    ): PositionMap {
        val oldMap = PositionMap(array.copy(data = array.data.copyOf()))

        val newMap = positionMap()
        val startIndex = when (direction) {
            Direction.LEFT, Direction.TOP -> 0
            Direction.RIGHT, Direction.BOTTOM -> 3
        }
        var columnRow = startIndex

        fun newPosition(line: Int) = when (direction) {
            Direction.LEFT -> Position(columnRow++, line)
            Direction.RIGHT -> Position(columnRow--, line)
            Direction.TOP -> Position(line, columnRow++)
            Direction.BOTTOM -> Position(line, columnRow--)
        }

        for (line in 0..3) {
            var curPos = oldMap.getNotEmptyPositionFrom(direction, line)
            columnRow = startIndex
            while (curPos != null) {
                val newPos = newPosition(line)
                val curId = oldMap[curPos.x, curPos.y]
                oldMap[curPos.x, curPos.y] = -1

                val nextPos = oldMap.getNotEmptyPositionFrom(direction, line)
                val nextId = nextPos?.let { oldMap[it.x, it.y] }
                //two blocks are equal
                if (nextId != null && board.getPower(curId) == board.getPower(nextId)) {
                    //merge these blocks
                    oldMap[nextPos.x, nextPos.y] = -1
                    newMap[newPos.x, newPos.y] = curId
                    merges += Triple(curId, nextId, newPos)
                } else {
                    //add old block
                    newMap[newPos.x, newPos.y] = curId
                    moves += Pair(curId, newPos)
                }
                curPos = oldMap.getNotEmptyPositionFrom(direction, line)
            }
        }
        return newMap
    }


    private fun getNotEmptyPositionFrom(direction: Direction, line: Int): Position? {
        when (direction) {
            Direction.LEFT -> for (i in 0..3) getOrNull(i, line)?.let { return it }
            Direction.RIGHT -> for (i in 3 downTo 0) getOrNull(i, line)?.let { return it }
            Direction.TOP -> for (i in 0..3) getOrNull(line, i)?.let { return it }
            Direction.BOTTOM -> for (i in 3 downTo 0) getOrNull(line, i)?.let { return it }
        }
        return null
    }

    private fun getOrNull(x: Int, y: Int) =
        if (array[x, y] != -1)
            Position(x, y)
        else
            null

    private fun getPower(x: Int, y: Int) =
        array.tryGet(x, y)?.let { blockId ->
            board.tryGetBlock(blockId)?.power?.power ?: -1
        } ?: -1

    operator fun get(x: Int, y: Int) = array[x, y]

    operator fun set(x: Int, y: Int, value: Int) {
        array[x, y] = value
    }

    fun getRandomFreePosition(): Position? {
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if (value == -1) {
                current++
                if (current == chosen) {
                    return Position(x, y)
                }
            }
        }
        return null
    }

    override fun equals(other: Any?): Boolean =
        (other is PositionMap) && this.array.data.contentEquals(other.array.data)

    override fun hashCode() = array.hashCode()
    fun hasAvailableMoves(): Boolean =
        array.any { x, y, _ -> hasAdjacentEqualPosition(x, y) }

    private fun hasAdjacentEqualPosition(x: Int, y: Int) =
        getPower(x, y)
            .let {
                it == getPower(x - 1, y) ||
                    it == getPower(x + 1, y) ||
                    it == getPower(x, y - 1) ||
                    it == getPower(x, y + 1)
            }

    fun powers() =
        IntArray(16) { getPower(it % 4, it / 4) }

    companion object {
        fun positionMap() = PositionMap()
    }
}

inline fun IntArray2.any(gen: (x: Int, y: Int, v: Int) -> Boolean): Boolean {
    each { x, y, v ->
        if (gen(x, y, v)) return true
    }
    return false
}
