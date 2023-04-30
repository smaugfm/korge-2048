package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.board.BoardMove
import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.TileIndex
import io.github.smaugfm.game2048.board.boardArraySize
import io.github.smaugfm.game2048.board.boardSize
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColorLight
import io.github.smaugfm.game2048.ui.UiBlock.Companion.addBlock
import korlibs.inject.AsyncInjector
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animate
import korlibs.korge.animate.block
import korlibs.korge.view.Container
import korlibs.korge.view.Views
import korlibs.korge.view.addTo
import korlibs.korge.view.graphics
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.math.geom.Scale
import korlibs.math.geom.Size

class UiBoard(
    virtualWidth: Int,
    private val uiConstants: UIConstants,
    private val gs: GameState,
) : Container() {
    private val blocks = arrayOfNulls<UiBlock>(boardArraySize)

    private val boardTopOffset: Double =
        uiConstants.statHeight + uiConstants.padding * 3 + uiConstants.buttonSize

    init {
        val boardSizePixels: Double =
            uiConstants.tilePadding * 5 + 4 * uiConstants.tileSize
        position((virtualWidth - boardSizePixels) / 2, boardTopOffset)
        roundRect(
            Size(boardSizePixels, boardSizePixels),
            uiConstants.rectCorners,
            backgroundColor,
        ) {
            graphics {
                repeat(boardSize) { i: Int ->
                    repeat(boardSize) { j: Int ->
                        fill(backgroundColorLight) {
                            roundRect(
                                uiConstants.tilePadding + i * (uiConstants.tilePadding + uiConstants.tileSize),
                                uiConstants.tilePadding + j * (uiConstants.tilePadding + uiConstants.tileSize),
                                uiConstants.tileSize,
                                uiConstants.tileSize,
                                uiConstants.rectRadius
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

    fun createNewBlock(power: Tile, index: TileIndex): UiBlock =
        addBlock(power, index, uiConstants, gs)
            .also {
                blocks[index] = it
            }

    suspend fun animate(
        boardMoves: List<BoardMove>,
        onEnd: () -> Unit,
    ): Animator {
        return animate {
            parallel {
                boardMoves.forEach {
                    when (it) {
                        is BoardMove.Move  -> {
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
        suspend fun Container.addBoard(injector: AsyncInjector): UiBoard =
            UiBoard(
                injector.get<Views>().virtualWidth,
                injector.get(),
                injector.get<GameState>()
            ).addTo(this)

        private operator fun Scale.plus(d: Double): Scale = Scale(scaleX + d, scaleY + d)
    }
}
