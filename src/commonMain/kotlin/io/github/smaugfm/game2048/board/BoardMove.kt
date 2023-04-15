package io.github.smaugfm.game2048.board

sealed interface BoardMove {
    data class Move(val from: Int, val to: Int) : BoardMove
    data class Merge(val from1: Int, val from2: Int, val to: Int, val newTile: Tile) : BoardMove
}
