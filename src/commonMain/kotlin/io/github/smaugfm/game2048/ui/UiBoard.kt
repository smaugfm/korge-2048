package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.core.MoveGenerator
import io.github.smaugfm.game2048.ui.UiBlock.Companion.addBlock
import io.github.smaugfm.game2048.core.PowerOfTwo
import io.github.smaugfm.game2048.board
import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.cellPadding
import io.github.smaugfm.game2048.cellSize
import io.github.smaugfm.game2048.rectCorners
import io.github.smaugfm.game2048.rectRadius
import korlibs.datastructure.iterators.fastForEach
import korlibs.image.color.Colors
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animate
import korlibs.korge.animate.block
import korlibs.korge.view.Container
import korlibs.korge.view.Stage
import korlibs.korge.view.addTo
import korlibs.korge.view.graphics
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.math.geom.Scale
import korlibs.math.geom.Size

class UiBoard(virtualWidth: Int) : Container() {
    private val blocks = arrayOfNulls<UiBlock>(boardArraySize)

    init {
        val boardSizePixels: Double = 50 + 4 * cellSize
        position((virtualWidth - boardSizePixels) / 2, 150.0)
        roundRect(
            Size(boardSizePixels, boardSizePixels),
            rectCorners,
            Colors["#b9aea0"]
        ) {
            graphics {
                for (i in 0 until boardSize) {
                    for (j in 0 until boardSize) {
                        fill(Colors["#cec0b2"]) {
                            roundRect(
                                cellPadding + i * (cellPadding + cellSize),
                                cellPadding + j * (cellPadding + cellSize),
                                cellSize,
                                cellSize,
                                rectRadius
                            )
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        blocks.indices.forEach(::deleteBlock)
    }

    fun createNewBlock(power: PowerOfTwo, index: Int) {
        blocks[index] = addBlock(power, index)
    }

    suspend fun animate(
        moves: List<MoveGenerator.Move>,
        onEnd: () -> Unit
    ) = animate {
        parallel {
            moves.fastForEach { (from, to, merge) ->
                animateMove(from, to)
                if (merge)
                    animateMerge(from, to)
            }
        }
        block {
            onEnd()
        }
    }

    private fun Animator.animateMove(
        from: Int,
        to: Int
    ) {
        val block = blocks[from]!!
        block.animateMove(this, to)
        deleteBlock(to)
        blocks[to] = block
    }

    private fun Animator.animateMerge(from: Int, to: Int) {
        sequence {
            block {
                val nextPower = board.power(from).next()
                deleteBlock(from)
                deleteBlock(to)
                createNewBlock(nextPower, to)
            }
            sequenceLazy {
                blocks[to]!!.animateScale(this)
            }
        }
    }

    private fun deleteBlock(index: Int) {
        val b = blocks[index]
        b?.removeFromParent()
        blocks[index] = null
    }

    companion object {
        fun Stage.addBoard() =
            UiBoard(views.virtualWidth)
                .addTo(this)

        private operator fun Scale.plus(d: Double): Scale =
            Scale(scaleX + d, scaleY + d)
    }
}
