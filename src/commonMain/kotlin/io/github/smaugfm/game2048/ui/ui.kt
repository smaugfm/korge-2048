package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.best
import io.github.smaugfm.game2048.isAiPlaying
import io.github.smaugfm.game2048.restart
import io.github.smaugfm.game2048.restoreField
import io.github.smaugfm.game2048.score
import io.github.smaugfm.game2048.ui.UIConstants.Companion.accentColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.gameOverTextColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelBackgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.rectCorners
import io.github.smaugfm.game2048.ui.UIConstants.Companion.textColor
import io.github.smaugfm.game2048.uiBoard
import io.github.smaugfm.game2048.uiConstants
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.keys
import korlibs.korge.input.onClick
import korlibs.korge.input.onDown
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.input.onUp
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Stage
import korlibs.korge.view.View
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.align.alignLeftToLeftOf
import korlibs.korge.view.align.alignRightToLeftOf
import korlibs.korge.view.align.alignRightToRightOf
import korlibs.korge.view.align.alignTopToBottomOf
import korlibs.korge.view.align.alignTopToTopOf
import korlibs.korge.view.align.centerOn
import korlibs.korge.view.align.centerXOn
import korlibs.korge.view.align.centerYOn
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.positionY
import korlibs.korge.view.roundRect
import korlibs.korge.view.setText
import korlibs.korge.view.size
import korlibs.korge.view.text
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size

fun Stage.addStaticUi() {
    val bgLogo = addLogo()
    val bgBest = addStat("BEST", best) {
        alignRightToRightOf(uiBoard)
        alignTopToTopOf(bgLogo)
    }
    addStat("SCORE", score) {
        alignRightToLeftOf(bgBest, 24)
        alignTopToTopOf(bgBest)
    }

    addButtons(bgBest, uiBoard)
}


private fun Stage.addButtons(
    bgBest: View,
    bgField: View,
) {
    val restartBlock = container {
        val bg = roundRect(
            Size(uiConstants.btnSize, uiConstants.btnSize),
            rectCorners,
            backgroundColor
        )
        alignTopToBottomOf(bgBest, 5)
        alignRightToRightOf(bgField)
        image(uiConstants.restartImg) {
            size(uiConstants.btnSize * 0.8, uiConstants.btnSize * 0.8)
            centerOn(bg)
        }
        onClick {
            if (!isAiPlaying.value)
                restart()
        }
    }
    val undoBlock = container {
        val bg = roundRect(
            Size(uiConstants.btnSize, uiConstants.btnSize),
            rectCorners,
            backgroundColor
        )
        alignTopToTopOf(restartBlock)
        alignRightToLeftOf(restartBlock, 5.0)
        image(uiConstants.undoImg) {
            size(uiConstants.btnSize * 0.6, uiConstants.btnSize * 0.6)
            centerOn(bg)
        }
        onClick {
            if (!isAiPlaying.value)
                restoreField(uiConstants.history.undo())
        }
    }
    container {
        val bg = roundRect(
            Size(uiConstants.btnSize, uiConstants.btnSize),
            rectCorners
        )
        updateAiBg(bg, isAiPlaying.value)

        alignTopToTopOf(undoBlock)
        alignRightToLeftOf(undoBlock, 5.0)
        text(
            "AI",
            (uiConstants.btnSize * 0.7).toFloat(),
            textColor,
            uiConstants.fontBold
        ) {
            centerXOn(bg)
            alignTopToTopOf(bg, -1)
        }
        onClick {
            isAiPlaying.update(!isAiPlaying.value)
        }
        isAiPlaying.observe {
            updateAiBg(bg, it)
        }
    }
}

private fun updateAiBg(bg: RoundRect, isAi: Boolean) {
    bg.fill = if (isAi)
        accentColor
    else
        backgroundColor
}

private fun Stage.addStat(
    label: String,
    prop: ObservableProperty<Int>,
    callback: @ViewDslMarker (RoundRect.() -> Unit) = {}
): RoundRect {
    val bgStat = roundRect(
        Size(uiConstants.cellSize * 1.5, uiConstants.cellSize * 0.8),
        rectCorners,
        labelBackgroundColor,
        callback = callback
    )
    text(label, uiConstants.cellSize.toFloat() * 0.2f, labelColor, uiConstants.font) {
        centerXOn(bgStat)
        alignTopToTopOf(bgStat, 5)
    }
    text(
        prop.value.toString(),
        uiConstants.cellSize.toFloat() * 0.35f,
        textColor,
        uiConstants.font
    ) {
        setTextBounds(
            Rectangle(
                0.0,
                0.0,
                bgStat.width.toDouble(),
                uiConstants.cellSize - 24.0
            )
        )
        alignment = TextAlignment.MIDDLE_CENTER
        alignTopToTopOf(bgStat, 14.0)
        centerXOn(bgStat)
        prop.observe {
            setText(it.toString())
        }
    }
    return bgStat
}

fun Container.showGameOver(onRestartClick: () -> Unit) = container {
    fun restart() {
        this@container.removeFromParent()
        onRestartClick()
    }
    position(uiBoard.pos)
    roundRect(uiBoard.size, rectCorners, Colors["#FFFFFF33"])
    text("Game Over", 60f, textColor, uiConstants.font) {
        centerOn(uiBoard)
        y -= 60
    }
    text("Try again", 40f, gameOverTextColor, uiConstants.font) {
        centerOn(uiBoard)
        y += 20
        onOver {
            onOver { color = RGBA(90, 90, 90) }
            onOut { color = RGBA(0, 0, 0) }
            onDown { color = RGBA(120, 120, 120) }
            onUp { color = RGBA(120, 120, 120) }
            onClick { restart() }
        }
    }
    keys.down {
        when (it.key) {
            Key.ENTER, Key.SPACE -> restart()
            else -> Unit
        }
    }
}


private fun Stage.addLogo(): RoundRect {
    val bgLogo = roundRect(
        Size(uiConstants.cellSize, uiConstants.cellSize),
        rectCorners,
        accentColor,
    ) {
        alignLeftToLeftOf(uiBoard)
        positionY(30)
    }
    text(
        "2048",
        uiConstants.cellSize.toFloat() * 0.4f,
        textColor,
        uiConstants.font,
    ) {
        centerYOn(bgLogo)
        alignLeftToLeftOf(bgLogo)
    }
    return bgLogo
}
