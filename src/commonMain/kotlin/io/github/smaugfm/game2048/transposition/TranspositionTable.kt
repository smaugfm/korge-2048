package io.github.smaugfm.game2048.transposition

import io.github.smaugfm.game2048.board.impl.Board4

interface TranspositionTable {
    val size: Int
    fun search(board: Board4): CacheEntry?
    fun update(board: Board4, depth: Int, score: Float)
    fun clear()

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
