package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.TileIndex
import io.github.smaugfm.game2048.boardSize
import io.github.smaugfm.game2048.ui.UIConstants.Companion.accentColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelBackgroundColor
import korlibs.image.color.Colors
import korlibs.io.async.ObservableProperty
import korlibs.korge.animate.Animator
import korlibs.korge.animate.moveTo
import korlibs.korge.animate.tween
import korlibs.korge.tween.get
import korlibs.korge.view.Container
import korlibs.korge.view.addTo
import korlibs.korge.view.align.centerBetween
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.text
import korlibs.math.geom.Scale
import korlibs.math.geom.Size
import korlibs.math.interpolation.Easing

class UiBlock(
    private val tile: Tile,
    private val uiConstants: UIConstants,
    private val animationSpeed: ObservableProperty<AnimationSpeed>
) : Container() {
    init {
        roundRect(
            Size(uiConstants.tileSize, uiConstants.tileSize),
            uiConstants.rectCorners,
            fill = color
        )
        val textColor =
            if (tile == Tile.TWO || tile == Tile.FOUR)
                Colors.BLACK
            else
                Colors.WHITE
        text(
            tile.score.toString(),
            textSize.toFloat(),
            textColor,
            uiConstants.font
        ) {
            centerBetween(0.0, 0.0, uiConstants.tileSize, uiConstants.tileSize)
        }
    }

    private val color
        get() = when (tile.power) {
            1 -> Colors["#F0E4DA"]
            2 -> Colors["#ECE0C9"]
            3 -> Colors["#FFB278"]
            4 -> Colors["#FE965C"]
            5 -> Colors["#F77B61"]
            6 -> Colors["#EB5837"]
            7 -> Colors["#F6DC92"]
            8 -> Colors["#F0D479"]
            9 -> Colors["#F4CE60"]
            10 -> Colors["#F8C847"]
            11 -> accentColor
            12 -> labelBackgroundColor
            13 -> Colors["#3B3631"]
            14 -> Colors["#332F2A"]
            15 -> Colors["#3D4354"]
            16 -> Colors["#343947"]
            else -> Colors["#252933"]
        }


    private val textSize
        get() =
            with(tile) {
                if (power <= 6) uiConstants.tileSize / 2
                else if (power <= 9) uiConstants.tileSize * 4 / 9
                else if (power <= 13) uiConstants.tileSize * 3 / 8
                else if (power <= 16) uiConstants.tileSize * 7 / 22
                else uiConstants.tileSize * 3 / 12
            }

    fun animateMove(animator: Animator, to: TileIndex) {
        animator.moveTo(
            this,
            columnX(to, uiConstants),
            rowY(to, uiConstants),
            animationSpeed.value.moveAnimationDuration,
            Easing.LINEAR
        )
    }

    fun animateScale(animator: Animator) {
        animator.tween(
            this::x[x - 4],
            this::y[y - 4],
            this::scale[scale + 0.1],
            time = animationSpeed.value.scaleAnimationDuration / 2,
            easing = Easing.LINEAR
        )
        animator.tween(
            this::x[x],
            this::y[y],
            this::scale[scale],
            time = animationSpeed.value.scaleAnimationDuration / 2,
            easing = Easing.LINEAR
        )
    }

    companion object {
        private operator fun Scale.plus(d: Double): Scale =
            Scale(scaleX + d, scaleY + d)

        fun columnX(tileIndex: TileIndex, uiConstants: UIConstants) =
            uiConstants.tilePadding + (uiConstants.tileSize + uiConstants.tilePadding) * (tileIndex % boardSize)

        fun rowY(tileIndex: TileIndex, uiConstants: UIConstants) =
            uiConstants.tilePadding + (uiConstants.tileSize + uiConstants.tilePadding) * (tileIndex / boardSize)

        fun Container.addBlock(
            power: Tile,
            index: TileIndex,
            uiConstants: UIConstants,
            animationSpeed: ObservableProperty<AnimationSpeed>
        ): UiBlock =
            UiBlock(power, uiConstants, animationSpeed)
                .addTo(this)
                .position(
                    columnX(index, uiConstants),
                    rowY(index, uiConstants)
                )
    }
}
