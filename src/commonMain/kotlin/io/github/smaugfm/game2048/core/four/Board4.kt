package io.github.smaugfm.game2048.core.four

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.Direction
import io.github.smaugfm.game2048.core.Direction.Companion.directions
import io.github.smaugfm.game2048.core.MoveBoardResult
import io.github.smaugfm.game2048.core.RandomBlockResult
import io.github.smaugfm.game2048.core.Tile
import io.github.smaugfm.game2048.core.TileIndex
import io.github.smaugfm.game2048.core.general.GeneralBoard
import korlibs.datastructure.IntArray2
import korlibs.datastructure.random.FastRandom

@OptIn(ExperimentalUnsignedTypes::class)
@JvmInline
value class Board4(val packed: ULong) : Board<Board4> {
    fun transpose(): Board4 {
        val x = this.packed
        val a1 = x and 0xF0F00F0FF0F00F0FUL
        val a2 = x and 0x0000F0F00000F0F0UL
        val a3 = x and 0x0F0F00000F0F0000UL
        val a = a1 or (a2 shl 12) or (a3 shr 12)
        val b1 = a and 0xFF00FF0000FF00FFUL
        val b2 = a and 0x00FF00FF00000000UL
        val b3 = a and 0x00000000FF00FF00UL
        return Board4(b1 or (b2 shr 24) or (b3 shl 24))
    }

    override fun hasAvailableMoves(): Boolean =
        directions.any {
            moveBoard(it) != this
        }

    override fun placeRandomBlock(): RandomBlockResult<Board4>? {
        val emptyTilesCount = countEmptyTiles()
        val randomIndex = FastRandom.Default.nextInt(emptyTilesCount)
        val tile = (if (FastRandom.nextDouble() < 0.9) Tile.TWO else Tile.FOUR)

        return iterateEveryEmptySpace(emptyTilesCount) { emptyIndex ->
            tile.takeIf { emptyIndex == randomIndex }
        }.firstOrNull()?.let { (newBoard, tileIndex) ->
            RandomBlockResult(newBoard, tile, tileIndex)
        }
    }

    override fun moveBoardGenerateMoves(direction: Direction): MoveBoardResult<Board4> =
        GeneralBoard(toIntArray())
            .moveBoardGenerateMoves(direction)
            .let {
                MoveBoardResult(
                    fromArray(it.board.array),
                    it.moves
                )
            }

    override fun moveBoard(
        direction: Direction
    ): Board4 = when (direction) {
        Direction.LEFT ->
            lookupRows(Tables4.leftLinesTable)

        Direction.RIGHT ->
            lookupRows(Tables4.rightLinesTable)

        Direction.TOP -> {
            val t = transpose()
            val result = t.lookupRows(Tables4.leftLinesTable)
            result.transpose()
        }

        Direction.BOTTOM -> {
            val t = transpose()
            val result = t.lookupRows(Tables4.rightLinesTable)
            result.transpose()
        }
    }

    private fun lookupRows(
        table: UShortArray,
    ): Board4 {
        var result = 0UL
        result = result or
            ((table[((packed shr 0) and ROW_MASK).toInt()].toULong()) shl 0)
        result = result or
            ((table[((packed shr 16) and ROW_MASK).toInt()].toULong()) shl 16)
        result = result or
            ((table[((packed shr 32) and ROW_MASK).toInt()].toULong()) shl 32)
        result = result or
            ((table[((packed shr 48) and ROW_MASK).toInt()].toULong()) shl 48)

        return Board4(result)
    }

    override fun toString(): String {
        return IntArray2(4, 4, toIntArray()).toString()
    }

    operator fun get(i: Int): ULong = (packed shr (i * 4)) and 0xFUL

    fun set(i: Int, value: Int): Board4 =
        Board4(packed or ((value.toULong() and 0xFUL) shl (i * 4)))

    override fun iterateEveryEmptySpace(
        emptyTilesCount: Int,
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<Pair<Board4, TileIndex>> {
        if (emptyTilesCount == 0)
            return emptySequence()

        return sequence {
            var temp = packed

            var i = 0
            var emptyIndex = 0
            while (true) {
                while (temp and 0xFUL != 0UL) {
                    temp = temp shr 4
                    i++
                }
                if (i >= 16)
                    break
                val tile = onEmpty(emptyIndex)
                if (tile != null)
                    yield(
                        Board4(temp or (tile.power.toULong() shl (4 * i))) to i
                    )

                temp = temp shr 4
                emptyIndex++
                i++
            }

        }
    }

    override fun countEmptyTiles(): Int {
        assert(packed != 0UL)

        var x = packed
        x = x or ((x shr 2) and 0x3333333333333333UL)
        x = x or (x shr 1)
        x = x.inv() and 0x1111111111111111UL
        x += x shr 32
        x += x shr 16
        x += x shr 8
        x += x shr 4
        return (x and 0xFUL).toInt()
    }


    companion object {
        private const val ROW_MASK = 0xFFFFUL

        fun Board4.toIntArray(): IntArray {
            val array = IntArray(16) { Tile.EMPTY.power }

            array.indices.forEach {
                array[it] = get(it).toInt()
            }
            return array
        }

        fun fromArray(array: IntArray): Board4 {
            var result = Board4(0UL)
            array.forEachIndexed { index, value ->
                if (value > 0)
                    result = result.set(index, value)
            }
            return result
        }
    }
}
