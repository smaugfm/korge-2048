package io.github.smaugfm.game2048.transposition

import co.touchlab.stately.collections.ConcurrentMutableMap

actual class ConcurrentHashMapTranspositionTable :
    MutableMapTranspositionTable(ConcurrentMutableMap())
