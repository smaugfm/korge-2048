package io.github.smaugfm.game2048.core.general

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.BoardMove
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.MoveBoardResult
import io.github.smaugfm.game2048.core.RandomBlockResult
import io.github.smaugfm.game2048.core.Tile
import io.github.smaugfm.game2048.core.TileIndex
import korlibs.datastructure.IntArray2
import korlibs.datastructure.random.FastRandom
import korlibs.datastructure.toIntList
import kotlin.math.roundToInt
import kotlin.math.sqrt

class GeneralBoard(
    val array: IntArray = IntArray(boardArraySize) { Tile.EMPTY.power }
) : Board<GeneralBoard> {
    private val emptyAddMove = { _: Int, _: Int -> }
    private val emptyAddMerge = { _: Int, _: Int, _: Int, _: Tile -> }
    private val indices = (0 until boardArraySize).toList().toIntArray()
    private val directionIndexesMap: Array<Array<IntArray>> =
        initDirectionIndexesMap()

    override fun placeRandomBlock(): RandomBlockResult<GeneralBoard>? {
        val index = powers()
            .withIndex()
            .filter { it.value.isEmpty }
            .randomOrNull()?.index ?: return null

        val tile = if (FastRandom.nextDouble() < 0.9) Tile.TWO else Tile.FOUR
        this[index] = tile

        return RandomBlockResult(this, tile, index)
    }

    override fun countEmptyTiles(): Int =
        array.count { Tile(it).isEmpty }

    override fun iterateEveryEmptySpace(
        emptyTilesCount: Int,
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<Pair<GeneralBoard, TileIndex>> {
        var emptyIndex = 0
        return indices.asSequence()
            .mapNotNull { i ->
                if (Tile(array[i]).isNotEmpty)
                    null
                else {
                    onEmpty(emptyIndex)?.let { tile ->
                        Pair(
                            GeneralBoard(array).also { it[i] = tile },
                            i
                        )
                    }.also {
                        emptyIndex++
                    }
                }
            }
    }

    override fun moveBoard(direction: Direction): GeneralBoard =
        moveBoardInternal(direction, emptyAddMove, emptyAddMerge)

    override fun moveBoardGenerateMoves(
        direction: Direction
    ): MoveBoardResult<GeneralBoard> {
        val moves = mutableListOf<BoardMove>()

        val newBoard = moveBoardInternal(
            direction,
            { from, to -> moves.add(BoardMove.Move(from, to)) },
            { from1, from2, to, newTile -> moves.add(BoardMove.Merge(from1, from2, to, newTile)) }
        )

        return MoveBoardResult(newBoard, moves)
    }

    private fun moveBoardInternal(
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ): GeneralBoard {
        val newBoard = GeneralBoard()

        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction.ordinal][i]
            moveLineLeftInternal(indexes, newBoard, addMove, addMerge)
        }

        return newBoard
    }

    fun moveLineLeftGenerateMoves(
        indexes: IntArray,
        newBoard: GeneralBoard,
        moves: MutableList<BoardMove>
    ) {
        moveLineLeftInternal(indexes, newBoard, { from, to ->
            moves.add(BoardMove.Move(from, to))
        },
            { from1, from2, to, newTile ->
                moves.add(BoardMove.Merge(from1, from2, to, newTile))
            }
        )
    }

    fun moveLineLeft(
        indexes: IntArray,
        newBoard: GeneralBoard,
    ) = moveLineLeftInternal(indexes, newBoard, emptyAddMove, emptyAddMerge)

    private fun moveLineLeftInternal(
        indexes: IntArray,
        newBoard: GeneralBoard,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ) {
        var cur1: Int? = firstNotEmpty(indexes) ?: return
        var cur2 = firstNotEmpty(indexes, cur1)
        var newCur = 0

        while (cur1 != null) {
            if (cur2 != null && this[indexes[cur1]] == this[indexes[cur2]]) {
                newBoard[indexes[newCur]] = this[indexes[cur1]].next()
                addMerge(
                    indexes[cur1],
                    indexes[cur2],
                    indexes[newCur],
                    newBoard[indexes[newCur]]
                )
                cur1 = firstNotEmpty(indexes, cur2)
                cur2 = firstNotEmpty(indexes, cur1)
                newCur++
            } else {
                newBoard[indexes[newCur]] = this[indexes[cur1]]
                if (cur1 != newCur)
                    addMove(
                        indexes[cur1],
                        indexes[newCur]
                    )
                newCur++
                if (cur2 != null) {
                    cur1 = cur2
                    cur2 = firstNotEmpty(indexes, cur2)
                } else {
                    return
                }
            }
        }
    }

    private fun firstNotEmpty(
        indexes: IntArray,
        startFrom: Int? = -1
    ): Int? {
        if (startFrom == null)
            return null
        var i = startFrom + 1
        while (i < indexes.size) {
            if (this[indexes[i]].isNotEmpty)
                return i
            i++
        }

        return null
    }

    private fun GeneralBoard.getXY(x: Int, y: Int): Tile {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return Tile.EMPTY
        return this[index]
    }

    private fun initDirectionIndexesMap(): Array<Array<IntArray>> =
        Direction.values().map { dir ->
            (0 until boardSize).map {
                when (dir) {
                    Direction.TOP ->
                        it until boardArraySize step boardSize

                    Direction.BOTTOM ->
                        (it until boardArraySize step boardSize).reversed()

                    Direction.LEFT ->
                        it * boardSize until ((it + 1) * boardSize)

                    Direction.RIGHT ->
                        (it * boardSize until ((it + 1) * boardSize)).reversed()
                }
                    .toList()
                    .toIntList()
                    .toIntArray()
            }.toTypedArray()
        }.toTypedArray()


    override fun hasAvailableMoves(): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(i)
        }

    private fun hasAdjacentEqualPosition(i: TileIndex): Boolean {
        val value = this[i]
        val x = i % boardSize
        val y = i / boardSize
        return value == this.getXY(x - 1, y) ||
            value == this.getXY(x + 1, y) ||
            value == this.getXY(x, y - 1) ||
            value == this.getXY(x, y + 1)
    }

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
        (other is GeneralBoard) && this.array.contentEquals(other.array)

    override fun hashCode() = array.hashCode()

    override fun toString() =
        IntArray2(
            sqrt(array.size.toDouble()).roundToInt(),
            sqrt(array.size.toDouble()).roundToInt(),
            array
        ).toString()
}
