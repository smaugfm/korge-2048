package io.github.smaugfm.game2048.expectimax.impl

import io.github.smaugfm.game2048.board.impl.Board4
import kotlin.jvm.JvmInline
import kotlin.random.Random

class TranspositionTable {
    companion object {
        private const val SIZE = (1 shl 22) // ~4.2m
        private val zMap = IntArray(256) {
            Random.nextInt(SIZE - 1)
        }
    }

    val table = LongArray(SIZE * 2)
    var size = 0
        private set

    inline fun search(board: Board4, consume: (depth: Int?, score: Float?) -> Unit) {
        val hash = zHash(board)
        if (table[hash * 2] == board.bits.toLong()) {
            val entry = CacheEntry(table[hash * 2 + 1])
            consume(entry.depth, entry.score)
        } else {
            consume(null, null)
        }
    }

    fun update(board: Board4, depth: Int, score: Float) {
        size++
        val hash = zHash(board)
        table[hash * 2] = board.bits.toLong()
        table[hash * 2 + 1] = CacheEntry.create(score, depth).bits
    }

    fun clear() {
        var i = 0
        size = 0
        while (i < SIZE * 2) {
            table[i] = 0L
            i++
        }
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

    @JvmInline
    value class CacheEntry(val bits: Long) {
        val score get() = Float.fromBits(bits.toInt())
        val depth get() = (bits ushr 32).toInt()

        companion object {
            fun create(score: Float, depth: Int): CacheEntry =
                CacheEntry(
                    (score.toRawBits().toULong() or (depth.toULong() shl 32)).toLong()
                )
        }
    }
}
