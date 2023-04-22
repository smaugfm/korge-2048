package io.github.smaugfm.game2048.board

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.board.impl.PrecomputedTables
import io.github.smaugfm.game2048.board.impl.PrecomputedTables.unpackArray
import io.github.smaugfm.game2048.board.impl.PrecomputedTables.xorSum
import io.github.smaugfm.game2048.board.impl.PrecomputedTables.zMapValue
import io.github.smaugfm.game2048.transposition.ZobristHashTranspositionTable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class PrecomputedTablesTest {
    @Test
    fun reverseLineTest() {
        assertEquals(0x123u.toUShort(), PrecomputedTables.reverseLine(0x3210u))
        assertEquals(0x0u.toUShort(), PrecomputedTables.reverseLine(0x0000u))
        assertEquals(0x1111u.toUShort(), PrecomputedTables.reverseLine(0x1111u))
        assertEquals(0x2211u.toUShort(), PrecomputedTables.reverseLine(0x1122u))
        assertEquals(0x2221u.toUShort(), PrecomputedTables.reverseLine(0x1222u))
    }

    @Test
    fun zHashTest() {
        Direction.values().forEach { dir ->
            repeat(10_000) {
                checkZhashUpdateTable(Board4(Random.nextLong().toULong()), dir)
            }
        }
    }

    @Test
    fun zHashTestSingle() {
        checkZhashUpdateTable(
            Board4.fromArray(
                intArrayOf(
                    5, 9, 1, 7,
                    12, 1, 5, 13,
                    7, 0, 11, 7,
                    14, 5, 4, 12,
                )
            ),
            Direction.TOP
        )
    }

    private fun checkZhashUpdateTable(board4: Board4, dir: Direction) {
        val movedBoard4 = board4.move(dir)
        val hash = ZobristHashTranspositionTable.zHash(board4)
        val updatedHash = ZobristHashTranspositionTable.zHash(movedBoard4)

        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            val update = (0 until 4).map { lineNum ->
                val line = (board4.bits shr (16 * lineNum)) and 0xFFFFu

                val board = AnySizeBoard.fromArray(
                    Board4(line shl (lineNum * 16)).toIntArray()
                )
                updateHashLeftRight(board, dir, lineNum, line)

            }.xorSum()
            val updatedPrecomputed = hash xor update
            assertEquals(
                updatedHash, updatedPrecomputed,
                "$dir\nInitial $hash\n$board4\nmoved $updatedHash - $updatedPrecomputed\n" +
                    "$movedBoard4\n"
            )
        } else {
            val t = board4.transpose()
            val update = (0 until 4).map { lineNum ->
                val line = (t.bits shr (16 * lineNum)) and 0xFFFFu

                val board = AnySizeBoard.fromArray(
                    Board4(line shl (lineNum * 16)).toIntArray()
                )
                updateHashUpDown(board, dir, lineNum, line)

            }.xorSum()
            val updatedPrecomputed = hash xor update
            assertEquals(
                updatedHash, updatedPrecomputed,
                "$dir\nInitial $hash\n$board4\nmoved $updatedHash - $updatedPrecomputed\n" +
                    "$movedBoard4\n"
            )
        }
    }

    private fun updateHashUpDown(
        board: AnySizeBoard,
        dir: Direction,
        lineNum: Int,
        line: ULong
    ): Int {
        val d = if (dir == Direction.TOP) Direction.LEFT else Direction.RIGHT
        val newBoard = board.move(d)

        val result = (lineNum * 4 until (lineNum * 4) + 4).map { i ->
            if (newBoard[i].power > 15)
                newBoard.array[i] = 15

            val originalI = (i % 4) * 4 + (i / 4)
            zMapValue(board[i], originalI) xor
                zMapValue(newBoard[i], originalI)
        }.xorSum()

        val table =
            if (dir == Direction.TOP)
                PrecomputedTables.zHashUpdateTableUp
            else
                PrecomputedTables.zHashUpdateTableDown

        val u = table[lineNum][line.toInt()]
        assertEquals(result, u)
        return result
    }

    private fun updateHashLeftRight(
        board: AnySizeBoard,
        dir: Direction,
        lineNum: Int,
        line: ULong
    ): Int {
        val newBoard = board.move(dir)

        val result = (lineNum * 4 until (lineNum * 4) + 4).map { i ->
            if (newBoard[i].power > 15)
                newBoard.array[i] = 15
            zMapValue(board[i], i) xor
                zMapValue(newBoard[i], i)
        }.xorSum()

        val table =
            if (dir == Direction.LEFT)
                PrecomputedTables.zHashUpdateTableLeft
            else
                PrecomputedTables.zHashUpdateTableRight

        val u = table[lineNum][line.toInt()]
        assertEquals(result, u)
        return u
    }

    @Test
    fun leftMovesTest() {
        movesTest(Direction.LEFT)
    }

    @Test
    fun rightMovesTest() {
        movesTest(Direction.RIGHT)
    }

    @Test
    fun upMovesTest() {
        movesTest(Direction.TOP)
    }

    @Test
    fun downMovesTest() {
        movesTest(Direction.BOTTOM)
    }

    private fun movesTest(dir: Direction) {
        for (line in (0u until 65536u)) {
            val lineArray = line.unpackArray()
            val arr = (lineArray + lineArray + lineArray + lineArray).toIntArray()

            val board4 = Board4.fromArray(arr)
            val board = AnySizeBoard.fromArray(arr)
            val newBoard = board.move(dir)
            newBoard.array.indices.forEach {
                if (newBoard.array[it] > 15)
                    newBoard.array[it] = 15
            }
            val actual = board4.move(dir)

            assertEquals(
                newBoard.tiles().toList(),
                actual.tiles().toList(),
                "initial\n$board4\n" +
                    "expected\n$newBoard\n" +
                    "actual\n $actual\n"
            )
        }
    }
}
