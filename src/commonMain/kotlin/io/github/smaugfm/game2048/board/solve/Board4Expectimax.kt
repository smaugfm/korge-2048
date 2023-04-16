package io.github.smaugfm.game2048.board.solve

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY

class Board4Expectimax(heuristics: Heuristics<Board4>, log: Boolean = true) :
    Expectimax<Board4>(heuristics, log) {
    override fun emptyTilesScoresSum(
        board: Board4,
        emptyTileProb: Double,
        depth: Int,
        maxDepth: Int
    ): Double {
        var sum = 0.0
        board.iterateEmptyTiles { tileIndex, _ ->
            val score2 = moveNode(
                board.placeTile(Tile.TWO, tileIndex),
                emptyTileProb * TILE_TWO_PROBABILITY,
                depth,
                maxDepth
            )
            val score4 = moveNode(
                board.placeTile(Tile.FOUR, tileIndex),
                emptyTileProb * TILE_FOUR_PROBABILITY,
                depth,
                maxDepth
            )

            sum += score2 * TILE_TWO_PROBABILITY + score4 * TILE_FOUR_PROBABILITY
        }

        return sum
    }
}
