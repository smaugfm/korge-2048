package io.github.smaugfm.game2048.transposition

import java.util.concurrent.ConcurrentHashMap

actual class ConcurrentHashMapTranspositionTable :
    MutableMapTranspositionTable(ConcurrentHashMap())
