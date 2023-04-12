package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.Position.Companion.toPosition

object MoveGenerator {

    private val indices = 0 until boardArraySize
    private val directionIndexesMap = mapOf(
        Direction.TOP to { i: Int -> i until boardArraySize step boardSize },
        Direction.BOTTOM to { i: Int -> (i until boardArraySize step boardSize).reversed() },
        Direction.LEFT to { i: Int -> i * boardSize until ((i + 1) * boardSize) },
        Direction.RIGHT to { i: Int -> (i * boardSize until ((i + 1) * boardSize)).reversed() },
    )

    data class Move(
        val from: Int, val to: Int, val merge: Boolean
    )

    data class MoveMapResult(
        val map: PositionMap,
        val moves: List<Move>,
    )

    fun moveMap(map: PositionMap, direction: Direction): MoveMapResult {
        val newMap = PositionMap()
        val moves = mutableListOf<Move>()
        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction]!!(i)
            moveMapLine(indexes, map, newMap, moves)
        }

        return MoveMapResult(newMap, moves)
    }

    fun moveMapLine(
        indexes: Iterable<Int>,
        map: PositionMap,
        newMap: PositionMap,
        moves: MutableList<Move>,
    ) {
        val newIndexes = indexes.iterator()
        var newMapIndex = newIndexes.next()
        var merged = false

        for (oldMapIndex in indexes) {
            val moving = map[oldMapIndex]
            if (moving <= 0)
                continue
            if (!merged && moving == newMap[newMapIndex]) {
                newMap[newMapIndex] += 1
                merged = true
            } else {
                if (newMap[newMapIndex] > 0)
                    newMapIndex = newIndexes.next()
                newMap[newMapIndex] = moving
                merged = false
            }
            if (oldMapIndex != newMapIndex) {
                moves.add(Move(oldMapIndex, newMapIndex, merged))
            }
        }
    }


    fun hasAvailableMoves(map: PositionMap): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(map, i)
        }

    private fun PositionMap.getXY(x: Int, y: Int): Int {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return -1
        return this[index]
    }

    private fun hasAdjacentEqualPosition(map: PositionMap, i: Int): Boolean {
        val value = map[i]
        val pos = i.toPosition()
        return value == map.getXY(pos.x - 1, pos.y) ||
            value == map.getXY(pos.x + 1, pos.y) ||
            value == map.getXY(pos.x, pos.y - 1) ||
            value == map.getXY(pos.x, pos.y + 1)
    }
}
