package io.github.smaugfm.game2048.transposition

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.transposition.TranspositionTable.CacheEntry

class HashMapTranspositionTable : TranspositionTable {
    private val table = HashMap<Long, Long>()

    override val size: Int
        get() = table.size

    override fun search(board: Board4): CacheEntry? {
        return table[board.bits.toLong()]?.let { CacheEntry(it) }
    }

    override fun update(board: Board4, depth: Int, score: Float) {
        table[board.bits.toLong()] = CacheEntry.create(score, depth).bits
    }

    override fun clear() {
        table.clear()
    }
}
