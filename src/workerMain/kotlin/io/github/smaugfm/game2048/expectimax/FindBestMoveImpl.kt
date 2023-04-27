package io.github.smaugfm.game2048.expectimax

import io.github.smaugfm.game2048.expectimax.FindBestMove.Companion.ScoreRequest

actual class FindBestMoveImpl actual constructor(log: Boolean) :
    FindBestMove() {
    override suspend fun scoreAllDirections(req: ScoreRequest):
        Pair<List<Companion.ScoreResult>, ExpectimaxDiagnostics?> {
        throw UnsupportedOperationException()
    }
}
