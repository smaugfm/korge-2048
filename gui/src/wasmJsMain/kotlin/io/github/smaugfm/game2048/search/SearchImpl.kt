package io.github.smaugfm.game2048.search

actual class SearchImpl actual constructor(log: Boolean) : Search() {
    public actual override suspend fun init() {
        throw UnsupportedOperationException("not supposed to be called")
    }

    actual override fun platformDepthLimit(distinctTiles: Int): Int {
        throw UnsupportedOperationException("not supposed to be called")
    }

    actual override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult> {
        throw UnsupportedOperationException("not supposed to be called")
    }

    actual override fun combineStats(one: SearchStats, two: SearchStats): SearchStats {
        throw UnsupportedOperationException("not supposed to be called")
    }
}
