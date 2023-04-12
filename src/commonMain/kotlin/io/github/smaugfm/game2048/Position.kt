package io.github.smaugfm.game2048

data class Position(
    val x: Int,
    val y: Int
) {
    companion object {
        fun Int.toPosition() = Position(this % boardSize, this / boardSize)
    }
}
