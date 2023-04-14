package io.github.smaugfm.game2048.ai

import io.github.smaugfm.game2048.core.Board
import io.github.smaugfm.game2048.core.GeneralBoard
import io.github.smaugfm.game2048.core.MoveBoardResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

interface Ai<T : Board> {
    suspend fun findBestMove(scope: CoroutineScope, board: T): Deferred<MoveBoardResult<T>?>

    companion object : Ai<GeneralBoard> by GeneralAi
}
