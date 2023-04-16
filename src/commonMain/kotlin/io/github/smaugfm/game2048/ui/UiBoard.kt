package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.TileIndex
import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColorLight
import io.github.smaugfm.game2048.ui.UIConstants.Companion.cellPadding
import io.github.smaugfm.game2048.ui.UIConstants.Companion.rectCorners
import io.github.smaugfm.game2048.ui.UIConstants.Companion.rectRadius
import io.github.smaugfm.game2048.ui.UiBlock.Companion.addBlock
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animate
import korlibs.korge.animate.block
import korlibs.korge.view.*
import korlibs.math.geom.Scale
import korlibs.math.geom.Size

class UiBoard(
    virtualWidth: Int,
    private val uiConstants: UIConstants
) : Container() {
    private val blocks = arrayOfNulls<UiBlock>(boardArraySize)

    init {
        val boardSizePixels: Double = 50 + 4 * uiConstants.cellSize
        position((virtualWidth - boardSizePixels) / 2, 150.0)
        roundRect(
            Size(boardSizePixels, boardSizePixels), rectCorners, backgroundColor,
        ) {
            graphics {
                repeat(boardSize) { i: Int ->
                    repeat(boardSize) { j: Int ->
                        fill(backgroundColorLight) {
                            roundRect(
                                cellPadding + i * (cellPadding + uiConstants.cellSize),
                                cellPadding + j * (cellPadding + uiConstants.cellSize),
                                uiConstants.cellSize,
                                uiConstants.cellSize,
                                rectRadius
                            )
                        }
                    }
                }
            }
        }
    }

    fun clear() {
        blocks.indices.forEach {
            blocks[it]?.removeFromParent()
            blocks[it] = null
        }
    }

    fun createNewBlock(power: Tile, index: TileIndex): UiBlock {
        return addBlock(power, index, uiConstants)
            .also {
                blocks[index] = it
            }
    }

    suspend fun animate(
        boardMoves: List<BoardMove>,
        onEnd: () -> Unit
    ): Animator {
        return animate {
            parallel {
                boardMoves.forEach {
                    when (it) {
                        is BoardMove.Move -> {
                            animateMove(it.from, it.to)
                        }

                        is BoardMove.Merge -> {
                            animateMerge(it.from1, it.from2, it.to, it.newTile)
                        }
                    }
                }
            }
            block {
                onEnd()
            }
        }
    }

    private fun Animator.animateMerge(from1: Int, from2: Int, to: Int, newTile: Tile) {
        sequence {
            parallel {
                blocks[from1]!!.animateMove(this, to)
                blocks[from2]!!.animateMove(this, to)
            }
            block {
                blocks[from1]!!.removeFromParent()
                blocks[from2]!!.removeFromParent()
                createNewBlock(newTile, to)
            }
            sequenceLazy {
                blocks[to]!!.animateScale(this)
            }
        }
    }

    private fun Animator.animateMove(
        from: Int,
        to: Int,
    ) {
        sequence {
            blocks[from]!!.animateMove(this, to)
            block {
                blocks[to] = blocks[from]
                blocks[from] = null
            }
        }
    }

    companion object {
        fun Container.addBoard(views: Views, uiConstants: UIConstants) =
            UiBoard(views.virtualWidth, uiConstants).addTo(this)

        private operator fun Scale.plus(d: Double): Scale = Scale(scaleX + d, scaleY + d)
    }
}
