package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable

expect class ExpectimaxImpl internal constructor(
    heuristics: Heuristics<Board4>,
    transpositionTable: TranspositionTable,
    log: Boolean = true,
) : Expectimax
