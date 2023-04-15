package io.github.smaugfm.game2048.board

import io.github.smaugfm.game2048.board.optimized.Board4
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class MoveGenerator4Test {

    @Test
    fun test() {
        val board = Board4.fromArray(
            intArrayOf(
                0, 2, 0, 0,
                1, 2, 0, 0,
                0, 2, 1, 0,
                0, 2, 0, 0,
            )
        )

        repeat(1000) {
            val (newBoard, power, index) = MoveGenerator4.placeRandomBlock(board) ?: fail()
            assertTrue {
                board[index] == 0UL
            }
            assertTrue {
                power == Tile.TWO || power == Tile.FOUR
            }
            assertTrue {
                ((newBoard.packed xor board.packed) shr (index * 4)) or 0xFUL == 0xFUL
            }
        }
    }
}
