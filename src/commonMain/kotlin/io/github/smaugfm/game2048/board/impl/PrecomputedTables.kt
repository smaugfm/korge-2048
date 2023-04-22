package io.github.smaugfm.game2048.board.impl

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.TileIndex
import io.github.smaugfm.game2048.heuristics.impl.AnySizeBoardHeuristics
import io.github.smaugfm.game2048.transposition.ZobristHashTranspositionTable.Companion.zMap

object PrecomputedTables {
    private const val COL_MASK = 0x000F000F000F000FUL
    private val firstLineLeftIndexes = intArrayOf(0, 1, 2, 3)
    private val anySizeHeuristics = AnySizeBoardHeuristics()

    val leftLinesTable = UShortArray(65536)
    val rightLinesTable = UShortArray(65536)
    val upLinesTable = ULongArray(65536)
    val downLinesTable = ULongArray(65536)
    val heuristicsTable = FloatArray(65536)

    val zHashUpdateTableLeft = Array(4) { IntArray(65536) }
    val zHashUpdateTableRight = Array(4) { IntArray(65536) }
    val zHashUpdateTableUp = Array(4) { IntArray(65536) }
    val zHashUpdateTableDown = Array(4) { IntArray(65536) }

    init {
        for (line in (0u until 65536u)) {
            val generalBoard = AnySizeBoard.fromArray(
                line.unpackArray().toIntArray()
            )

            val newBoard = AnySizeBoard.fromArray(
                intArrayOf(0, 0, 0, 0)
            )

            generalBoard.moveLineToStart(
                firstLineLeftIndexes, newBoard
            )

            initMoveTables(newBoard, line)
            initHeuristicsTable(newBoard, line)
            initZobristUpdateTable(line)
        }
    }

    private fun initMoveTables(newBoard: AnySizeBoard, line: UInt) {
        val result = newBoard.array.map {
            //4 bits per tile, so maximum tile is 32768
            if (it > 15) 15 else it
        }.toIntArray().toUIntArray().packArray().toUShort()

        val reverseResult = reverseLine(result)
        val reverseLine = reverseLine(line.toUShort())

        leftLinesTable[line.toInt()] = result
        rightLinesTable[reverseLine.toInt()] = reverseResult
        upLinesTable[line.toInt()] = transposeColumn(result)
        downLinesTable[reverseLine.toInt()] = transposeColumn(reverseResult)
    }

    private fun initHeuristicsTable(newBoard: AnySizeBoard, line: UInt) {
        val score = anySizeHeuristics.evaluateLine(newBoard, firstLineLeftIndexes)
        heuristicsTable[line.toInt()] = score
    }

    private fun initZobristUpdateTable(line: UInt) {
        Direction.values().zip(
            listOf(
                zHashUpdateTableLeft,
                zHashUpdateTableRight,
                zHashUpdateTableUp,
                zHashUpdateTableDown
            )
        ).forEach { (dir, table) ->
            repeat(4) { lineNum ->
                when (dir) {
                    Direction.LEFT, Direction.RIGHT -> {
                        table[lineNum][line.toInt()] =
                            updateHashLeftRight(line, dir, lineNum)
                    }

                    Direction.TOP, Direction.BOTTOM -> {
                        table[lineNum][line.toInt()] =
                            updateHashUpDown(line, dir, lineNum)
                    }
                }
            }
        }
    }

    private fun updateHashLeftRight(
        line: UInt, dir: Direction, lineNum: Int
    ): Int {
        val board = AnySizeBoard.fromArray(
            Board4(line.toULong() shl (lineNum * 16)).toIntArray()
        )
        val newBoard = board.move(dir)

        return (lineNum * 4 until (lineNum * 4) + 4).map { i ->
            if (newBoard[i].power > 15) newBoard.array[i] = 15
            zMapValue(board[i], i) xor zMapValue(newBoard[i], i)
        }.xorSum()
    }

    private fun updateHashUpDown(
        line: UInt, dir: Direction, lineNum: Int
    ): Int {
        val d = if (dir == Direction.TOP) Direction.LEFT else Direction.RIGHT
        val board = AnySizeBoard.fromArray(
            Board4(line.toULong() shl (lineNum * 16)).toIntArray()
        )
        val newBoard = board.move(d)

        return (lineNum * 4 until (lineNum * 4) + 4).map { i ->
            if (newBoard[i].power > 15)
                newBoard.array[i] = 15

            val originalI = (i % 4) * 4 + (i / 4)
            zMapValue(board[i], originalI) xor
                zMapValue(newBoard[i], originalI)
        }.xorSum()
    }

    fun zMapValue(
        tile: Tile, i: TileIndex
    ): Int {
        val v = tile.power.toUInt() and 0xfu
        val ix = (i shl 4)

        val zMapIndex = ix or v.toInt()
        return zMap[zMapIndex]
    }

    private fun transposeColumn(line: UShort): ULong {
        val tmp = line.toULong()
        return (tmp or (tmp shl 12) or (tmp shl 24) or (tmp shl 36)) and COL_MASK
    }

    internal fun UInt.unpackArray() = uintArrayOf(
        (this shr 0) and 0xFu,
        (this shr 4) and 0xFu,
        (this shr 8) and 0xFu,
        (this shr 12) and 0xFu,
    )

    private fun UIntArray.packArray() =
        ((this[0] and 0xFu) shl 0) or ((this[1] and 0xFu) shl 4) or ((this[2] and 0xFu) shl 8) or ((this[3] and 0xFu) shl 12)

    internal fun reverseLine(l: UShort): UShort {
        val line = l.toUInt()
        val result =
            (line shr 12) or ((line shr 4) and 0x00F0u) or ((line shl 4) and 0x0F00u) or (line shl 12)
        return result.toUShort()
    }

    fun List<Int>.xorSum() = fold(0) { x, y -> x xor y }

}
