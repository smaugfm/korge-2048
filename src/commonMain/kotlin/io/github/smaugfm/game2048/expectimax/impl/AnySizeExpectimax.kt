package io.github.smaugfm.game2048.expectimax.impl

import io.github.smaugfm.game2048.board.impl.AnySizeBoard
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_FOUR_PROBABILITY
import io.github.smaugfm.game2048.board.Tile.Companion.TILE_TWO_PROBABILITY
import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics

class AnySizeExpectimax(heuristics: Heuristics<AnySizeBoard>, log: Boolean = true) :
    Expectimax<AnySizeBoard>(heuristics, log) {

    override fun emptyTilesScoresSum(
        board: AnySizeBoard,
        emptyTileProb: Double,
        depth: Int,
        maxDepth: Int
    ): Double {
        var sum = 0.0
        repeat(boardArraySize) { i ->
            val tile = board[i]
            if (tile.isNotEmpty)
                return@repeat

            val scoreBoard2 = moveNode(
                board.placeTile(Tile.TWO, i),
                emptyTileProb * TILE_TWO_PROBABILITY,
                depth,
                maxDepth,
            ) * TILE_TWO_PROBABILITY
            val scoreBoard4 = moveNode(
                board.placeTile(Tile.FOUR, i),
                emptyTileProb * TILE_FOUR_PROBABILITY,
                depth,
                maxDepth
            ) * TILE_FOUR_PROBABILITY

            sum += scoreBoard2 + scoreBoard4
        }

        return sum
    }
}
