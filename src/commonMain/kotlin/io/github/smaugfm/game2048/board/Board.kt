package io.github.smaugfm.game2048.board

typealias TileIndex = Int

interface Board<out T : Board<T>> {

    fun hasAvailableMoves(): Boolean
    fun moveBoard(direction: Direction): T
    fun moveBoardGenerateMoves(direction: Direction): MoveBoardResult<T>
    fun placeRandomBlock(): RandomBlockResult<T>?

    fun countEmptyTiles(): Int
    fun iterateEveryEmptySpace(
        emptyTilesCount: Int = this.countEmptyTiles(),
        onEmpty: (emptySpaceIndex: Int) -> Tile?
    ): Sequence<Pair<T, TileIndex>>

    fun evaluate(): Double
    fun evaluateLine(indexes: IntArray): Double

    companion object {
        const val SCORE_POW = 3.5
        const val SCORE_WEIGHT = 11
        const val MONO_POW = 4.0
        const val MONO_WEIGHT = 47.0
        const val MERGES_WEIGHT = 700.0f
        const val EMPTY_WEIGHT = 270.0f
        const val LOST_PENALTY = 200000.0f
    }
}
