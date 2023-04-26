package io.github.smaugfm.game2048.transposition

import java.util.concurrent.ConcurrentHashMap

class ConcurrentHashMapTranspositionTable :
    MutableMapTranspositionTable(ConcurrentHashMap())
