package io.github.smaugfm.game2048.search

expect class SearchImpl(log: Boolean = true) : Search {
    override fun platformDepthLimit(distinctTiles: Int): Int
    override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult>
    override fun combineStats(one: SearchStats, two: SearchStats): SearchStats
}
