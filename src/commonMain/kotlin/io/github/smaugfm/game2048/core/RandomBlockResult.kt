package io.github.smaugfm.game2048.core

data class RandomBlockResult<T : Board>(
    val board: T,
    val power: Tile,
    val index: TileIndex,
)
