import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.onClick
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Stage
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
import korlibs.korge.view.graphics
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.korge.view.roundRect
import korlibs.korge.view.setText
import korlibs.korge.view.size
import korlibs.korge.view.text
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size

fun Stage.addButtons(
    bgBest: RoundRect,
    bgField: RoundRect,
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
            this@addButtons.restart()
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
            this@addButtons.restoreField(history.undo())
        }
    }
}

fun Stage.addStat(
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

fun Stage.addField(): RoundRect {
    val bgField = roundRect(
        Size(fieldSize, fieldSize),
        rectCorners,
        Colors["#b9aea0"]
    ) {
        position(leftIndent, topIndent)
        graphics {
            for (i in 0 until 4) {
                for (j in 0 until 4) {
                    fill(Colors["#cec0b2"]) {
                        roundRect(
                            10.0 + i * (10 + cellSize),
                            10.0 + j * (10 + cellSize),
                            cellSize,
                            cellSize,
                            rectRadius
                        )
                    }
                }
            }
        }
    }
    return bgField
}

fun Stage.addLogo(): RoundRect {
    val bgLogo = roundRect(
        Size(cellSize, cellSize),
        rectCorners,
        Colors["#edc403"]
    ) {
        position(leftIndent, 30.0)
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
