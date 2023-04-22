package io.github.smaugfm.game2048.transposition

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.transposition.TranspositionTable.CacheEntry
import kotlin.random.Random

class ZobristHashTranspositionTable : TranspositionTable {
    companion object {
        const val ZOBRIST_TABLE_SIZE = (1 shl 22) // ~4.2m
        private val rand = Random(1337)

        val zMap = IntArray(256) {
            rand.nextInt(1, ZOBRIST_TABLE_SIZE - 1)
        }

        fun zHash(board: Board4): Int {
            var x = board.bits
            var hash = 0
            var i = 0
            while (i < 16) {
                val v = x and 0xfu
                val ix = (i shl 4)
                val zMapIndex = ix or v.toInt()
                hash = hash xor zMap[zMapIndex]
                x = x shr 4
                i++
            }
            return hash
        }
    }

    private val table = LongArray(ZOBRIST_TABLE_SIZE * 2)

    override var size = 0
        private set

    override fun search(board: Board4): CacheEntry? {
        val hash = zHash(board)
        return if (table[hash * 2] == board.bits.toLong()) {
            CacheEntry(table[hash * 2 + 1])
        } else {
            null
        }
    }

    override fun update(board: Board4, depth: Int, score: Float) {
        size++
        val hash = zHash(board)
        table[hash * 2] = board.bits.toLong()
        table[hash * 2 + 1] = CacheEntry.create(score, depth).bits
    }

    override fun clear() {
        var i = 0
        size = 0
        while (i < ZOBRIST_TABLE_SIZE * 2) {
            table[i] = 0L
            i++
        }
    }
}
