package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Board4

actual class SearchImpl actual constructor(log: Boolean) : Search() {
    actual override suspend fun calculateBoardScore(
        board: Board4,
        depthLimit: Int
    ): List<SearchResult> {
        throw UnsupportedOperationException("not supposed to be called")
    }

    public actual override suspend fun init() {
        throw UnsupportedOperationException("not supposed to be called")
    }

    actual override fun platformDepthLimit(distinctTiles: Int): Int {
        throw UnsupportedOperationException("not supposed to be called")
    }

    actual override fun combineStats(one: SearchStats, two: SearchStats): SearchStats {
        throw UnsupportedOperationException("not supposed to be called")
    }
}
