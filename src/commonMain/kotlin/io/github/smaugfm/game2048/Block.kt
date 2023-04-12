package io.github.smaugfm.game2048

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.animate.Animator
import korlibs.korge.animate.moveTo
import korlibs.korge.animate.tween
import korlibs.korge.tween.get
import korlibs.korge.view.Container
import korlibs.korge.view.addTo
import korlibs.korge.view.centerBetween
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.Scale
import korlibs.math.geom.Size
import korlibs.math.interpolation.Easing
import korlibs.time.seconds

class Block(val power: PowerOfTwo) : Container() {
    init {
        roundRect(Size(cellSize, cellSize), rectCorners, fill = color)
        val textColor =
            if (power.isLowest)
                Colors.BLACK
            else
                Colors.WHITE
        text(
            power.score.toString(),
            textSize.toFloat(),
            textColor,
            font
        ) {
            centerBetween(0.0, 0.0, cellSize, cellSize)
        }
    }

    private val color
        get() = when (power.power) {
            1 -> RGBA(240, 228, 218)
            2 -> RGBA(236, 224, 201)
            3 -> RGBA(255, 178, 120)
            4 -> RGBA(254, 150, 92)
            5 -> RGBA(247, 123, 97)
            6 -> RGBA(235, 88, 55)
            7 -> RGBA(236, 220, 146)
            8 -> RGBA(240, 212, 121)
            9 -> RGBA(244, 206, 96)
            10 -> RGBA(248, 200, 71)
            11 -> RGBA(256, 194, 46)
            12 -> RGBA(104, 130, 249)
            13 -> RGBA(51, 85, 247)
            14 -> RGBA(10, 47, 222)
            15 -> RGBA(9, 43, 202)
            16 -> RGBA(181, 37, 188)
            17 -> RGBA(166, 34, 172)
            else -> RGBA(166, 34, 172)
        }


    private val textSize
        get() =
            with(power) {
                if (power <= 6) cellSize / 2
                else if (power <= 9) cellSize * 4 / 9
                else if (power <= 13) cellSize * 2 / 5
                else if (power <= 16) cellSize * 7 / 20
                else cellSize * 3 / 10
            }

    fun animateMove(animator: Animator, to: Position) =
        animator.moveTo(this, to.columnX, to.rowY, 0.15.seconds, Easing.LINEAR)

    fun animateScale(animator: Animator) {
        animator.tween(
            this::x[x - 4],
            this::y[y - 4],
            this::scale[scale + 0.1],
            time = 0.1.seconds,
            easing = Easing.LINEAR
        )
        animator.tween(
            this::x[x],
            this::y[y],
            this::scale[scale],
            time = 0.1.seconds,
            easing = Easing.LINEAR
        )
    }


    companion object {
        private val Position.columnX get() = cellPadding + (cellSize + cellPadding) * x
        private val Position.rowY get() = cellPadding + (cellSize + cellPadding) * y

        private operator fun Scale.plus(d: Double): Scale =
            Scale(scaleX + d, scaleY + d)

        fun Container.addBlock(power: PowerOfTwo, pos: Position) =
            Block(power)
                .addTo(this)
                .position(
                    pos.columnX,
                    pos.rowY,
                )
    }
}
