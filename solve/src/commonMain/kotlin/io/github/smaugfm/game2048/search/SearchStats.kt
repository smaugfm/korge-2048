package io.github.smaugfm.game2048.search

interface SearchStats {
    val cacheSize: Int
    val evaluations: Long
    val moves: Long
    val cacheHits: Long
    val maxDepthReached: Int
}
