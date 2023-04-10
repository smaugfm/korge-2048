package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.Block.Companion.addBlock
import korlibs.image.color.Colors
import korlibs.korge.animate.Animator
import korlibs.korge.animate.animate
import korlibs.korge.animate.block
import korlibs.korge.animate.moveTo
import korlibs.korge.animate.tween
import korlibs.korge.tween.get
import korlibs.korge.view.Container
import korlibs.korge.view.Stage
import korlibs.korge.view.addTo
import korlibs.korge.view.graphics
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.math.geom.Scale
import korlibs.math.geom.Size
import korlibs.math.interpolation.Easing
import korlibs.time.seconds
import kotlin.collections.set

class Board(virtualWidth: Int) : Container() {
    private val blocks = mutableMapOf<Int, Block>()
    private var freeId = 0

    init {
        val boardSize: Double = 50 + 4 * cellSize
        position((virtualWidth - boardSize) / 2, 150.0)
        roundRect(
            Size(boardSize, boardSize),
            rectCorners,
            Colors["#b9aea0"]
        ) {
            graphics {
                for (i in 0 until 4) {
                    for (j in 0 until 4) {
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

    fun getPower(id: Int) = tryGetBlock(id)!!.power
    fun tryGetBlock(id: Int) = blocks[id]

    fun clear() {
        blocks.values.forEach { it.removeFromParent() }
        blocks.clear()
        freeId = 0
    }

    fun createNewBlock(power: PowerOfTwo, position: Position): Int {
        val id = freeId++
        createNewBlockWithId(id, power, position)
        return id
    }

    private fun createNewBlockWithId(id: Int, power: PowerOfTwo, pos: Position) {
        blocks[id] = addBlock(power)
            .position(
                pos.columnX,
                pos.rowY
            )
    }

    private fun getBlock(id: Int) = tryGetBlock(id)!!
    private fun deleteBlock(blockId: Int) =
        blocks.remove(blockId)!!.removeFromParent()

    suspend fun animate(
        moves: List<Pair<Int, Position>>,
        merges: List<Triple<Int, Int, Position>>,
        onEnd: () -> Unit
    ) = animate {
        parallel {
            moves.forEach { (id, pos) ->
                moveTo(board.getBlock(id), pos.columnX, pos.rowY, 0.15.seconds, Easing.LINEAR)
            }
            merges.forEach { (id1, id2, pos) ->
                sequence {
                    parallel {
                        moveTo(
                            board.getBlock(id1),
                            pos.columnX,
                            pos.rowY,
                            0.15.seconds,
                            Easing.LINEAR
                        )
                        moveTo(
                            board.getBlock(id2),
                            pos.columnX,
                            pos.rowY,
                            0.15.seconds,
                            Easing.LINEAR
                        )
                    }
                    block {
                        val nextPower = board.getPower(id1).next()
                        board.deleteBlock(id1)
                        board.deleteBlock(id2)
                        board.createNewBlockWithId(id1, nextPower, pos)
                    }
                    sequenceLazy {
                        animateScale(board.getBlock(id1))
                    }
                }
            }
        }
        block {
            onEnd()
        }
    }

    companion object {
        fun Stage.addBoard() =
            Board(views.virtualWidth)
                .addTo(this)

        private operator fun Scale.plus(d: Double): Scale =
            Scale(scaleX + d, scaleY + d)

        private fun Animator.animateScale(block: Block) {
            val x = block.x
            val y = block.y
            val scale = block.scale
            tween(
                block::x[x - 4],
                block::y[y - 4],
                block::scale[scale + 0.1],
                time = 0.1.seconds,
                easing = Easing.LINEAR
            )
            tween(
                block::x[x],
                block::y[y],
                block::scale[scale],
                time = 0.1.seconds,
                easing = Easing.LINEAR
            )
        }


        private val Position.columnX get() = cellPadding + (cellSize + cellPadding) * x
        private val Position.rowY get() = cellPadding + (cellSize + cellPadding) * y
    }
}
