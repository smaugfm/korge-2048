package io.github.smaugfm.game2048.search

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.board.impl.Board4

data class SearchRequest(
    val board: Board4,
    val depthLimit: Int,
    val dir: Direction,
) {
    fun serialize(): String =
        "${board.bits}|$depthLimit|$dir"

    companion object {
        fun deserialize(str: String?): SearchRequest? {
            if (str == null || str == "null")
                return null

            val (bits, depthLimit, dir) = str.split("|")
            return SearchRequest(
                Board4(bits.toULong()),
                depthLimit.toInt(),
                Direction.valueOf(dir)
            )
        }
    }
}
