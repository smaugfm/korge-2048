package io.github.smaugfm.game2048.transposition

actual class ConcurrentHashMapTranspositionTable actual constructor() :
    MutableMapTranspositionTable(HashMap())
