package io.github.smaugfm.game2048.core

data class MoveBoardResult<T : Board>(
    val board: T,
    val moves: List<BoardMove>,
)
