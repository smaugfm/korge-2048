package io.github.smaugfm.game2048.board

@OptIn(ExperimentalUnsignedTypes::class)
object PrecomputedTables4 {
    val leftLinesTable = UShortArray(65536) { 0u }
    val rightLinesTable = UShortArray(65536) { 0u }

    init {
        for (line in (0u until 65536u)) {
            val generalBoard = AnySizeBoard(
                line.unpackArray().toIntArray()
            )

            val newBoard = AnySizeBoard(
                intArrayOf(0, 0, 0, 0)
            )
            generalBoard.moveLineLeft(
                intArrayOf(0, 1, 2, 3),
                newBoard
            )

            val result = newBoard.array.toUIntArray().packArray().toUShort()

            val reverseResult = reverseLine(result)
            val reverseLine = reverseLine(line.toUShort())

            leftLinesTable[line.toInt()] = result
            rightLinesTable[reverseLine.toInt()] = reverseResult
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
