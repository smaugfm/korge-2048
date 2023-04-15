package io.github.smaugfm.game2048.core

data class RandomBlockResult<out T: Board<T>>(
    val newBoard: T,
    val power: Tile,
    val index: TileIndex,
)
