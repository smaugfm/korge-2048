package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.Direction.Companion.directions
import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import korlibs.io.concurrent.createFixedThreadDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove(log) {
    private val table = ConcurrentHashMapTranspositionTable()

    override suspend fun scoreAllDirections(
        req: ScoreRequest
    ): List<Expectimax.ExpectimaxResult?> {
        table.clear()

        return directions.map {
            scope.async {
                Expectimax(it, table)
                    .score(req.board, req.depthLimit)
            }
        }.awaitAll()
    }

    companion object {
        private val dispatcher: CoroutineDispatcher =
            Dispatchers.createFixedThreadDispatcher("expectimax", Direction.values().size)
        private val scope = CoroutineScope(dispatcher)
    }
}
