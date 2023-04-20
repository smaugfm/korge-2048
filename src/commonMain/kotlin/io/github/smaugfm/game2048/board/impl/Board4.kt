package io.github.smaugfm.game2048.board.impl

import io.github.smaugfm.game2048.board.*
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import korlibs.datastructure.IntArray2
import korlibs.datastructure.random.FastRandom
import korlibs.io.lang.assert
import kotlin.jvm.JvmInline

@JvmInline
value class Board4 private constructor(val bits: ULong) : Board<Board4> {
    fun transpose(): Board4 {
        val a1 = bits and 0xF0F00F0FF0F00F0FUL
        val a2 = bits and 0x0000F0F00000F0F0UL
        val a3 = bits and 0x0F0F00000F0F0000UL
        val a = a1 or (a2 shl 12) or (a3 shr 12)
        val b1 = a and 0xFF00FF0000FF00FFUL
        val b2 = a and 0x00FF00FF00000000UL
        val b3 = a and 0x00000000FF00FF00UL
        return Board4(b1 or (b2 shr 24) or (b3 shl 24))
    }

    override fun hasAvailableMoves(): Boolean =
        directions.any {
            move(it) != this
        }

    override fun placeRandomTile(): TilePlacementResult<Board4>? {
        val emptyTilesCount = if (bits == 0UL) 16 else countEmptyTiles()
        val randomIndex = FastRandom.Default.nextInt(emptyTilesCount)
        val tile = Tile.randomNewTile()

        iterateEmptyTiles { index, emptyIndex ->
            if (emptyIndex == randomIndex) {
                return TilePlacementResult(
                    placeTile(tile, index),
                    tile,
                    index
                )
            }
        }

        return null
    }

    override fun placeTile(tile: Tile, i: TileIndex): Board4 =
        Board4(bits or (tile.power.toULong() shl (4 * i)))

    override fun tiles(): Array<Tile> {
        val array = Array(16) { Tile.EMPTY }

        array.indices.forEach {
            array[it] = Tile(get(it).toInt())
        }
        return array
    }

    fun toIntArray(): IntArray {
        return tiles().map { it.power }.toIntArray()
    }


    override fun moveGenerateMoves(direction: Direction): MoveBoardResult<Board4> =
        AnySizeBoard.fromArray(toIntArray())
            .moveGenerateMoves(direction)
            .let {
                MoveBoardResult(
                    fromArray(it.board.array),
                    it.moves
                )
            }

    override fun move(
        direction: Direction
    ): Board4 = when (direction) {
        Direction.LEFT ->
            lookupRows(PrecomputedBoard4Tables.leftLinesTable)

        Direction.RIGHT ->
            lookupRows(PrecomputedBoard4Tables.rightLinesTable)

        Direction.TOP ->
            lookupColumns(PrecomputedBoard4Tables.upLinesTable)

        Direction.BOTTOM ->
            lookupColumns(PrecomputedBoard4Tables.downLinesTable)
    }

    private fun lookupColumns(table: ULongArray): Board4 {
        val t = transpose().bits
        var ret = 0UL
        ret = ret or (table[((t shr 0) and ROW_MASK).toInt()] shl 0)
        ret = ret or (table[((t shr 16) and ROW_MASK).toInt()] shl 4)
        ret = ret or (table[((t shr 32) and ROW_MASK).toInt()] shl 8)
        ret = ret or (table[((t shr 48) and ROW_MASK).toInt()] shl 12)

        return Board4(ret)
    }

    private fun lookupRows(table: UShortArray): Board4 {
        var result = 0UL
        result = result or
            ((table[((bits shr 0) and ROW_MASK).toInt()].toULong()) shl 0)
        result = result or
            ((table[((bits shr 16) and ROW_MASK).toInt()].toULong()) shl 16)
        result = result or
            ((table[((bits shr 32) and ROW_MASK).toInt()].toULong()) shl 32)
        result = result or
            ((table[((bits shr 48) and ROW_MASK).toInt()].toULong()) shl 48)

        return Board4(result)
    }

    override fun toString(): String {
        return IntArray2(4, 4, toIntArray()).toString()
    }

    operator fun get(i: Int): ULong = (bits shr (i * 4)) and 0xFUL

    fun set(i: Int, value: Int): Board4 =
        Board4(bits or ((value.toULong() and 0xFUL) shl (i * 4)))

    inline fun iterateEmptyTiles(
        onEmpty: (tileIndex: TileIndex, emptyTileIndex: TileIndex) -> Unit,
    ) {
        var temp = bits

        var i = 0
        var emptyIndex = 0
        while (true) {
            while (temp and 0xFUL != 0UL) {
                temp = temp shr 4
                i++
            }
            if (i >= 16)
                break
            onEmpty(i, emptyIndex)

            temp = temp shr 4
            emptyIndex++
            i++
        }
    }

    override fun countEmptyTiles(): Int {
        assert(bits != 0UL)

        var x = bits
        x = x or ((x shr 2) and 0x3333333333333333UL)
        x = x or (x shr 1)
        x = x.inv() and 0x1111111111111111UL
        x += x shr 32
        x += x shr 16
        x += x shr 8
        x += x shr 4
        return (x and 0xFUL).toInt()
    }

    fun countDistinctTiles(): Int {
        return tiles().toList()
            .filter { it.isNotEmpty }
            .distinctBy { it.power }
            .size
    }

    companion object : BoardFactory<Board4> {
        const val ROW_MASK = 0xFFFFUL

        override fun createEmpty(): Board4 =
            Board4(0UL)

        override fun fromTiles(tiles: Array<Tile>): Board4 =
            fromArray(tiles.map { it.power }.toIntArray())

        override fun fromArray(tiles: IntArray): Board4 {
            var result = Board4(0UL)
            tiles.forEachIndexed { index, value ->
                if (value > 0)
                    result = result.set(index, value)
            }
            return result
        }
    }
}
