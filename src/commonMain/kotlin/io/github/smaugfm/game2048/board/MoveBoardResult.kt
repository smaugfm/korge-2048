package io.github.smaugfm.game2048.board

data class MoveBoardResult<out T: Board<T>>(
    val board: T,
    val moves: List<BoardMove>,
)
