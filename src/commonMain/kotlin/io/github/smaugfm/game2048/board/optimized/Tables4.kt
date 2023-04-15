package io.github.smaugfm.game2048.board.optimized

import io.github.smaugfm.game2048.board.AnySizeBoard

@OptIn(ExperimentalUnsignedTypes::class)
object Tables4 {
    val leftLinesTable = UShortArray(65536) { 0u }
    val rightLinesTable = UShortArray(65536) { 0u }

    init {
        for (line in (0u until 65536u)) {
            val generalBoard = AnySizeBoard(
                uintArrayOf(
                    (line shr 0) and 0xFu,
                    (line shr 4) and 0xFu,
                    (line shr 8) and 0xFu,
                    (line shr 12) and 0xFu,
                ).toIntArray()
            )

            val newBoard = AnySizeBoard(
                intArrayOf(0, 0, 0, 0)
            )
            generalBoard.moveLineLeft(
                intArrayOf(0, 1, 2, 3),
                newBoard
            )

            val result = (((newBoard.array[0].toUInt() and 0xFu) shl 0) or
                ((newBoard.array[1].toUInt() and 0xFu) shl 4) or
                ((newBoard.array[2].toUInt() and 0xFu) shl 8) or
                ((newBoard.array[3].toUInt() and 0xFu) shl 12))
                .toUShort()
            val reverseResult = reverseLine(result)
            val reverseLine = reverseLine(line.toUShort())

            leftLinesTable[line.toInt()] = result
            rightLinesTable[reverseLine.toInt()] = reverseResult
        }
    }

    fun reverseLine(l: UShort): UShort {
        val line = l.toUInt()
        val result =
            (line shr 12) or ((line shr 4) and 0x00F0u) or ((line shl 4) and 0x0F00u) or (line shl 12)
        return result.toUShort()
    }
}
