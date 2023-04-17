package io.github.smaugfm.game2048.board.impl

import io.github.smaugfm.game2048.heuristics.impl.NneonneoAnySizeHeuristics

@OptIn(ExperimentalUnsignedTypes::class)
object PrecomputedTables4 {
    private val firstLineLeftIndexes = intArrayOf(0, 1, 2, 3)
    private val anySizeHeuristics = NneonneoAnySizeHeuristics()

    val leftLinesTable = UShortArray(65536) { 0u }
    val rightLinesTable = UShortArray(65536) { 0u }
    val heuristicsTable = DoubleArray(65536) { 0.0 }

    init {
        for (line in (0u until 65536u)) {
            val generalBoard = AnySizeBoard.fromArray(
                line.unpackArray().toIntArray()
            )

            val newBoard = AnySizeBoard.fromArray(
                intArrayOf(0, 0, 0, 0)
            )
            generalBoard.moveLineToStart(
                firstLineLeftIndexes,
                newBoard
            )

            val result = newBoard.array.toUIntArray().packArray().toUShort()

            val reverseResult = reverseLine(result)
            val reverseLine = reverseLine(line.toUShort())

            leftLinesTable[line.toInt()] = result
            rightLinesTable[reverseLine.toInt()] = reverseResult

            val score = anySizeHeuristics.evaluateLine(newBoard, firstLineLeftIndexes)
            heuristicsTable[line.toInt()] = score
        }
    }

    fun UInt.unpackArray() = uintArrayOf(
        (this shr 0) and 0xFu,
        (this shr 4) and 0xFu,
        (this shr 8) and 0xFu,
        (this shr 12) and 0xFu,
    )

    fun UIntArray.packArray() =
        ((this[0] and 0xFu) shl 0) or
            ((this[1] and 0xFu) shl 4) or
            ((this[2] and 0xFu) shl 8) or
            ((this[3] and 0xFu) shl 12)

    fun reverseLine(l: UShort): UShort {
        val line = l.toUInt()
        val result =
            (line shr 12) or ((line shr 4) and 0x00F0u) or ((line shl 4) and 0x0F00u) or (line shl 12)
        return result.toUShort()
    }
}
