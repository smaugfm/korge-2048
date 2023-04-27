package io.github.smaugfm.game2048.expectimax

actual class FindBestMoveImpl actual constructor(log: Boolean) : FindBestMove() {
    override suspend fun scoreAllDirections(req: Companion.ScoreRequest): Pair<List<Companion.ScoreResult>, ExpectimaxDiagnostics?> {
        TODO("Not yet implemented")
    }
}
