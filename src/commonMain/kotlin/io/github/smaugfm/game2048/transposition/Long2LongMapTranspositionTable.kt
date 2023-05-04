package io.github.smaugfm.game2048.transposition

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.util.hash.Long2LongOpenHashMap

class Long2LongMapTranspositionTable : TranspositionTable {
    private val table = Long2LongOpenHashMap(16384)

    override val size: Int
        get() = table.size

    override fun search(board: Board4): TranspositionTable.CacheEntry {
        val result = table[board.bits.toLong()]
        if (result == Long2LongOpenHashMap.defRetValue)
            return TranspositionTable.CacheEntry.EMPTY

        return TranspositionTable.CacheEntry(result)
    }

    override fun update(board: Board4, depth: Int, score: Float) {
        table.put(
            board.bits.toLong(),
            TranspositionTable.CacheEntry.create(score, depth).bits
        )
    }

    override fun clear() {
        table.clear()
    }

}
