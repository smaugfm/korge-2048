package io.github.smaugfm.game2048.board

data class TilePlacementResult<out T : Board<T>>(
    val newBoard: T,
    val tile: Tile,
    val index: TileIndex,
)
