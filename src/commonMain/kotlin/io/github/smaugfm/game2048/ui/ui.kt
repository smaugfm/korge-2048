package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.accentColor
import io.github.smaugfm.game2048.backgroundColor
import io.github.smaugfm.game2048.best
import io.github.smaugfm.game2048.btnSize
import io.github.smaugfm.game2048.cellSize
import io.github.smaugfm.game2048.font
import io.github.smaugfm.game2048.fontBold
import io.github.smaugfm.game2048.history
import io.github.smaugfm.game2048.isAiPlaying
import io.github.smaugfm.game2048.labelColor
import io.github.smaugfm.game2048.rectCorners
import io.github.smaugfm.game2048.restart
import io.github.smaugfm.game2048.restartImg
import io.github.smaugfm.game2048.restoreField
import io.github.smaugfm.game2048.score
import io.github.smaugfm.game2048.textColor
import io.github.smaugfm.game2048.uiBoard
import io.github.smaugfm.game2048.undoImg
import korlibs.image.text.TextAlignment
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.onClick
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Stage
import korlibs.korge.view.View
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.alignLeftToLeftOf
import korlibs.korge.view.alignRightToLeftOf
import korlibs.korge.view.alignRightToRightOf
import korlibs.korge.view.alignTopToBottomOf
import korlibs.korge.view.alignTopToTopOf
import korlibs.korge.view.centerOn
import korlibs.korge.view.centerXOn
import korlibs.korge.view.centerYOn
import korlibs.korge.view.container
import korlibs.korge.view.image
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
        val bg = roundRect(Size(btnSize, btnSize), rectCorners, backgroundColor)
        alignTopToBottomOf(bgBest, 5)
        alignRightToRightOf(bgField)
        image(restartImg) {
            size(btnSize * 0.8, btnSize * 0.8)
            centerOn(bg)
        }
        onClick {
            if (!isAiPlaying.value)
                restart()
        }
    }
    val undoBlock = container {
        val bg = roundRect(Size(btnSize, btnSize), rectCorners, backgroundColor)
        alignTopToTopOf(restartBlock)
        alignRightToLeftOf(restartBlock, 5.0)
        image(undoImg) {
            size(btnSize * 0.6, btnSize * 0.6)
            centerOn(bg)
        }
        onClick {
            if (!isAiPlaying.value)
                restoreField(history.undo())
        }
    }
    val aiBlock = container {
        val bg = roundRect(
            Size(btnSize, btnSize),
            rectCorners
        )
        updateAiBg(bg, isAiPlaying.value)

        alignTopToTopOf(undoBlock)
        alignRightToLeftOf(undoBlock, 5.0)
        text("AI", (btnSize * 0.7).toFloat(), textColor, fontBold) {
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
        Size(cellSize * 1.5, cellSize * 0.8),
        rectCorners,
        backgroundColor,
        callback = callback
    )
    text(label, cellSize.toFloat() * 0.2f, labelColor, font) {
        centerXOn(bgStat)
        alignTopToTopOf(bgStat, 5)
    }
    text(prop.value.toString(), cellSize.toFloat() * 0.35f, textColor, font) {
        setTextBounds(Rectangle(0.0, 0.0, bgStat.width.toDouble(), cellSize - 24.0))
        alignment = TextAlignment.MIDDLE_CENTER
        alignTopToTopOf(bgStat, 14.0)
        centerXOn(bgStat)
        prop.observe {
            setText(it.toString())
        }
    }
    return bgStat
}


private fun Stage.addLogo(): RoundRect {
    val bgLogo = roundRect(
        Size(cellSize, cellSize),
        rectCorners,
        accentColor,
    ) {
        alignLeftToLeftOf(uiBoard)
        positionY(30)
    }
    text(
        "2048",
        cellSize.toFloat() * 0.4f,
        textColor,
        font,
    ) {
        centerYOn(bgLogo)
        alignLeftToLeftOf(bgLogo)
    }
    return bgLogo
}
