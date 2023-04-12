package io.github.smaugfm.game2048

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
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
    val bgBest = addLabel("BEST", best) {
        alignRightToRightOf(board)
        alignTopToTopOf(bgLogo)
    }
    addLabel("SCORE", score) {
        alignRightToLeftOf(bgBest, 24)
        alignTopToTopOf(bgBest)
    }

    addButtons(bgBest, board)
}


private fun Stage.addButtons(
    bgBest: View,
    bgField: View,
) {
    val restartBlock = container {
        val bg = roundRect(Size(btnSize, btnSize), rectCorners, RGBA(185, 174, 160))
        alignTopToBottomOf(bgBest, 5)
        alignRightToRightOf(bgField)
        image(restartImg) {
            size(btnSize * 0.8, btnSize * 0.8)
            centerOn(bg)
        }
        onClick {
            restart()
        }
    }
    container {
        val bg = roundRect(Size(btnSize, btnSize), rectCorners, RGBA(185, 174, 160))
        alignTopToTopOf(restartBlock)
        alignRightToLeftOf(restartBlock, 5.0)
        image(undoImg) {
            size(btnSize * 0.6, btnSize * 0.6)
            centerOn(bg)
        }
        onClick {
            restoreField(history.undo())
        }
    }
}

private fun Stage.addLabel(
    label: String,
    prop: ObservableProperty<Int>,
    callback: @ViewDslMarker (RoundRect.() -> Unit) = {}
): RoundRect {
    val bgStat = roundRect(
        Size(cellSize * 1.5, cellSize * 0.8),
        rectCorners,
        Colors["#bbae9e"],
        callback = callback
    )
    text(label, cellSize.toFloat() * 0.2f, RGBA(239, 226, 210), font) {
        centerXOn(bgStat)
        alignTopToTopOf(bgStat, 5)
    }
    text(prop.value.toString(), cellSize.toFloat() * 0.35f, Colors.WHITE, font) {
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
        Colors["#edc403"]
    ) {
        alignLeftToLeftOf(board)
        positionY(30)
    }
    text(
        "2048",
        cellSize.toFloat() * 0.4f,
        Colors.WHITE,
        font,
    ) {
        centerYOn(bgLogo)
        alignLeftToLeftOf(bgLogo)
    }
    return bgLogo
}
