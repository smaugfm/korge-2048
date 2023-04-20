package io.github.smaugfm.game2048.transposition

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.transposition.TranspositionTable.CacheEntry
import kotlin.random.Random

class ZobristHashTranspositionTable : TranspositionTable {
    companion object {
        private const val SIZE = (1 shl 22) // ~4.2m
        private val zMap = IntArray(256) {
            Random.nextInt(SIZE - 1)
        }

        fun zHash(board: Board4): Int {
            var x = board.bits
            var hash = 0
            var i = 0
            while (i < 16) {
                hash = hash xor zMap[(i shl 4) or (x and 0xfu).toInt()]
                x = x shr 4
                i++
            }
            return hash
        }

    }

    private val table = LongArray(SIZE * 2)

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
        while (i < SIZE * 2) {
            table[i] = 0L
            i++
        }
    }
}
