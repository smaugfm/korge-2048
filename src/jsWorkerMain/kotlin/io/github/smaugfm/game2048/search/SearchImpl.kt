package io.github.smaugfm.game2048.search

actual class SearchImpl actual constructor(log: Boolean) :
    Search() {
    override val distinctTilesDepthNegativeTerm
        get() =
            throw UnsupportedOperationException()

    override suspend fun getExpectimaxResults(requests: List<SearchRequest>): List<SearchResult> {
        throw UnsupportedOperationException()
    }

    override fun combineStats(one: SearchStats, two: SearchStats): SearchStats {
        throw UnsupportedOperationException()
    }
}
