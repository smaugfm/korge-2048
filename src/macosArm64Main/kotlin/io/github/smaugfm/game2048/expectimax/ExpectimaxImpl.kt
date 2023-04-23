package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.transposition.TranspositionTable

actual class ExpectimaxImpl internal actual constructor(
    heuristics: Heuristics<Board4>,
    transpositionTable: TranspositionTable,
    log: Boolean
) : Expectimax(heuristics, transpositionTable, log)
