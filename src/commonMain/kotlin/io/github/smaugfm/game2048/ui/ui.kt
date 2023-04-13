package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.*
import korlibs.image.text.TextAlignment
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.onClick
import korlibs.korge.view.*
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
            rectCorners,
            backgroundColor
        )
        alignTopToTopOf(undoBlock)
        alignRightToLeftOf(undoBlock, 5.0)
        text("AI", (btnSize * 0.7).toFloat(), textColor, fontBold) {
            centerXOn(bg)
            alignTopToTopOf(bg, -1)
        }
        onClick {
            isAiPlaying.update(!isAiPlaying.value)

            bg.fill = if (isAiPlaying.value)
                accentColor
            else
                backgroundColor
        }
    }
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
