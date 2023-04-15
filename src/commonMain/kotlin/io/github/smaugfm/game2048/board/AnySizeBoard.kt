package io.github.smaugfm.game2048.board

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.board.Board.Companion.EMPTY_WEIGHT
import io.github.smaugfm.game2048.board.Board.Companion.MERGES_WEIGHT
import io.github.smaugfm.game2048.board.Board.Companion.MONO_POW
import io.github.smaugfm.game2048.board.Board.Companion.MONO_WEIGHT
import io.github.smaugfm.game2048.board.Board.Companion.SCORE_POW
import io.github.smaugfm.game2048.board.Board.Companion.SCORE_WEIGHT
import korlibs.datastructure.IntArray2
import korlibs.datastructure.random.FastRandom
import korlibs.datastructure.toIntList
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AnySizeBoard(
    val array: IntArray = IntArray(boardArraySize) { Tile.EMPTY.power }
) : Board<AnySizeBoard> {
    private val emptyAddMove = { _: Int, _: Int -> }
    private val emptyAddMerge = { _: Int, _: Int, _: Int, _: Tile -> }
    private val indices = (0 until boardArraySize).toList().toIntArray()

    fun powers() = array.map(::Tile).toTypedArray()

    override fun hasAvailableMoves(): Boolean =
        indices.any { i ->
            hasAdjacentEqualPosition(i)
        }

    override fun moveBoard(direction: Direction): AnySizeBoard =
        moveBoardInternal(direction, emptyAddMove, emptyAddMerge)

    override fun moveBoardGenerateMoves(
        direction: Direction
    ): MoveBoardResult<AnySizeBoard> {
        val moves = mutableListOf<BoardMove>()

        val newBoard = moveBoardInternal(
            direction,
            { from, to -> moves.add(BoardMove.Move(from, to)) },
            { from1, from2, to, newTile -> moves.add(BoardMove.Merge(from1, from2, to, newTile)) }
        )

        return MoveBoardResult(newBoard, moves)
    }

    override fun placeRandomBlock(): RandomBlockResult<AnySizeBoard>? {
        val index = powers()
            .withIndex()
            .filter { it.value.isEmpty }
            .randomOrNull()?.index ?: return null

        val tile = if (FastRandom.nextDouble() < 0.9) Tile.TWO else Tile.FOUR
        this.array[index] = tile.power

        return RandomBlockResult(this, tile, index)
    }

    override fun countEmptyTiles(): Int =
        array.count { Tile(it).isEmpty }

    override fun iterateEveryEmptySpace(
        emptyTilesCount: Int,
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<Pair<AnySizeBoard, TileIndex>> {
        var emptyIndex = 0
        return indices.asSequence()
            .mapNotNull { i ->
                if (Tile(array[i]).isNotEmpty)
                    null
                else {
                    onEmpty(emptyIndex)?.let { tile ->
                        Pair(
                            AnySizeBoard(array).also {
                                it.array[i] = tile.power
                            },
                            i
                        )
                    }.also {
                        emptyIndex++
                    }
                }
            }
    }

    override fun evaluate(): Double =
        (0 until boardSize).sumOf { i ->
            evaluateLine(directionIndexesMap[Direction.LEFT.ordinal][i]) +
                evaluateLine(directionIndexesMap[Direction.TOP.ordinal][i])
        }

    override fun evaluateLine(indexes: IntArray): Double {
        var empty = 0
        var merges = 0
        var score = 0.0

        var prevTile: Tile? = null
        var rowMerges = 0
        var monoLeft = 0.0
        var monoRight = 0.0

        for (index in indexes) {
            val tile = Tile(this.array[index])
            score += tile.power.toDouble().pow(SCORE_POW)
            if (tile.isEmpty) {
                empty++
            } else {
                if (prevTile == tile) {
                    rowMerges++
                } else if (rowMerges > 0) {
                    merges += 1 + rowMerges;
                    rowMerges = 0;
                }
                if (prevTile != null) {
                    if (prevTile.power > tile.power) {
                        monoLeft += (prevTile.power.toDouble().pow(MONO_POW) - tile.power.toDouble().pow(MONO_POW))
                    } else {
                        monoRight += (tile.power.toDouble().pow(MONO_POW) - prevTile.power.toDouble().pow(MONO_POW))
                    }
                }

                prevTile = tile
            }
        }
        if (rowMerges > 0)
            merges += 1 + rowMerges

        return EMPTY_WEIGHT * empty + MERGES_WEIGHT * merges + MONO_WEIGHT *
            min(monoLeft, monoRight) + SCORE_WEIGHT * score
    }

    fun moveLineLeft(
        indexes: IntArray,
        newBoard: AnySizeBoard,
    ) = moveLineLeftInternal(indexes, newBoard, emptyAddMove, emptyAddMerge)

    fun moveLineLeftGenerateMoves(
        indexes: IntArray,
        newBoard: AnySizeBoard,
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

    private fun moveLineLeftInternal(
        indexes: IntArray,
        newBoard: AnySizeBoard,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ) {
        var cur1: Int? = firstNotEmpty(indexes) ?: return
        var cur2 = firstNotEmpty(indexes, cur1)
        var newCur = 0

        while (cur1 != null) {
            if (cur2 != null && Tile(this.array[indexes[cur1]]) == Tile(this.array[indexes[cur2]])) {
                newBoard.array[indexes[newCur]] = Tile(this.array[indexes[cur1]]).next().power
                addMerge(
                    indexes[cur1],
                    indexes[cur2],
                    indexes[newCur],
                    Tile(newBoard.array[indexes[newCur]])
                )
                cur1 = firstNotEmpty(indexes, cur2)
                cur2 = firstNotEmpty(indexes, cur1)
                newCur++
            } else {
                newBoard.array[indexes[newCur]] = Tile(this.array[indexes[cur1]]).power
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

    private fun moveBoardInternal(
        direction: Direction,
        addMove: (from: Int, to: Int) -> Unit,
        addMerge: (from1: Int, from2: Int, to: Int, newTile: Tile) -> Unit,
    ): AnySizeBoard {
        val newBoard = AnySizeBoard()

        for (i in (0 until boardSize)) {
            val indexes = directionIndexesMap[direction.ordinal][i]
            moveLineLeftInternal(indexes, newBoard, addMove, addMerge)
        }

        return newBoard
    }

    private fun firstNotEmpty(
        indexes: IntArray,
        startFrom: Int? = -1
    ): Int? {
        if (startFrom == null)
            return null
        var i = startFrom + 1
        while (i < indexes.size) {
            if (Tile(this.array[indexes[i]]).isNotEmpty)
                return i
            i++
        }

        return null
    }


    private fun hasAdjacentEqualPosition(i: TileIndex): Boolean {
        val value = Tile(this.array[i])
        val x = i % boardSize
        val y = i / boardSize
        return value == this.getXY(x - 1, y) ||
            value == this.getXY(x + 1, y) ||
            value == this.getXY(x, y - 1) ||
            value == this.getXY(x, y + 1)
    }

    private fun AnySizeBoard.getXY(x: Int, y: Int): Tile {
        val index = y * boardSize + x
        if (index < 0 || index >= boardSize)
            return Tile.EMPTY
        return Tile(this.array[index])
    }


    override fun equals(other: Any?): Boolean =
        (other is AnySizeBoard) && this.array.contentEquals(other.array)

    override fun hashCode() = array.hashCode()

    override fun toString() =
        IntArray2(
            sqrt(array.size.toDouble()).roundToInt(),
            sqrt(array.size.toDouble()).roundToInt(),
            array
        ).toString()

    companion object {
        private val directionIndexesMap: Array<Array<IntArray>> =
            initDirectionIndexesMap()

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
    }
}
