package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Board4

expect class SearchImpl(log: Boolean = true) : Search {

    public override suspend fun init()
    override fun platformDepthLimit(distinctTiles: Int): Int
    override suspend fun calculateBoardScore(board: Board4, depthLimit: Int)
        : List<SearchResult>

    override fun combineStats(one: SearchStats, two: SearchStats): SearchStats
}
