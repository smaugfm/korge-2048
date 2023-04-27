package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.ui.UIConstants.Companion.accentColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.gameOverTextColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelBackgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.textColor
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.TextAlignment
import korlibs.inject.AsyncInjector
import korlibs.io.async.ObservableProperty
import korlibs.io.lang.format
import korlibs.korge.input.keys
import korlibs.korge.input.onClick
import korlibs.korge.input.onDown
import korlibs.korge.input.onOut
import korlibs.korge.input.onOver
import korlibs.korge.input.onUp
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
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
import kotlin.math.roundToInt

class StaticUi(
    private val gs: GameState,
    private val inputManager: KorgeInputManager,
    private val uiConstants: UIConstants,
) {
    private val buttonSize: Double = uiConstants.tileSize * 0.3
    private val padding = uiConstants.tileSize / 10
    private val statMargin = padding
    private val statInnerPadding = uiConstants.tileSize / 8

    fun addStaticUi(
        container: Container,
        uiBoard: UiBoard,
    ) {
        with(container) {
            val bgLogo = addLogo(uiBoard)
            val bgBest = addStat("BEST", gs.best) {
                alignRightToRightOf(uiBoard)
                alignTopToTopOf(bgLogo)
            }
            addStat("SCORE", gs.score) {
                alignRightToLeftOf(bgBest, statMargin)
                alignTopToTopOf(bgBest)
            }

            addButtons(bgBest, uiBoard)
            addMoveLabel(uiBoard)
            addAiLabels(uiBoard)
        }
    }

    private fun Container.addAiLabels(uiBoard: UiBoard) {
        val depthText = addUnderBoardLabel(gs.aiDepth, { "depth %d".format(it) }) {
            alignRightToRightOf(uiBoard)
            alignTopToBottomOf(uiBoard, padding / 2)
        }
        addUnderBoardLabel(gs.aiElapsedMs, {
            if (it > 1)
                "%dms".format(it.roundToInt())
            else
                "0ms"
        }) {
            alignRightToLeftOf(depthText, padding / 2)
            alignTopToBottomOf(uiBoard, padding / 2)
        }
    }

    private fun Container.addMoveLabel(uiBoard: UiBoard) {
        addUnderBoardLabel(
            gs.moveNumber,
            { "move #%d".format(it) }
        ) {
            alignLeftToLeftOf(uiBoard)
            alignTopToBottomOf(uiBoard, padding / 2)
        }
    }

    private fun <T> Container.addUnderBoardLabel(
        prop: ObservableProperty<T>,
        format: (T) -> String,
        align: Text.() -> Unit,
    ): Text {
        fun Text.update(propValue: T) {
            text = format(propValue)
        }
        return text(
            "",
            (buttonSize * 0.5).toFloat(),
            UIConstants.underboardLabelColor,
            uiConstants.fontBold
        ) {
            update(prop.value)
            prop.observe {
                update(it)
                align()
            }
            align()
        }
    }

    private fun Container.addButtons(
        bgBest: View,
        bgField: View,
    ) {
        val restartBlock = addBtn(bgBest, inputManager::handleRestartClick) { bg ->
            alignRightToRightOf(bgField)
            image(uiConstants.restartImg) {
                size(buttonSize * 0.8, buttonSize * 0.8)
                centerOn(bg)
            }
        }
        val undoBlock = addBtn(bgBest, inputManager::handleUndoClick) { bg ->
            alignRightToLeftOf(restartBlock, padding / 2)
            image(uiConstants.undoImg) {
                size(buttonSize * 0.6, buttonSize * 0.6)
                centerOn(bg)
            }
        }
        val aiBlock = addBtn(bgBest, inputManager::handleAiClick) { bg ->
            fun onAiPlaying(bg: RoundRect, isAi: Boolean) {
                bg.fill = if (isAi)
                    accentColor
                else
                    backgroundColor
            }
            onAiPlaying(bg, gs.isAiPlaying.value)
            alignRightToLeftOf(undoBlock, padding / 2)
            text(
                "AI",
                (buttonSize * 0.7).toFloat(),
                textColor,
                uiConstants.fontBold
            ) {
                centerXOn(bg)
                alignTopToTopOf(bg, -1)
            }
            gs.isAiPlaying.observe {
                onAiPlaying(bg, it)
            }
        }
        addBtn(bgBest, inputManager::handleAnimationSpeedClick) { bg ->
            fun Text.onAnimationSpeed(speed: AnimationSpeed) {
                text = when (speed) {
                    AnimationSpeed.Fast -> "x2"
                    AnimationSpeed.Faster -> "x3"
                    AnimationSpeed.Normal -> "x1"
                }
            }

            this.visible = gs.isAiPlaying.value
            gs.isAiPlaying.observe {
                this.visible = it
            }
            alignRightToLeftOf(aiBlock, padding / 2)
            text(
                "AI",
                (buttonSize * 0.7).toFloat(),
                textColor,
                uiConstants.fontBold
            ) {
                onAnimationSpeed(gs.aiAnimationSpeed.value)
                centerXOn(bg)
                alignTopToTopOf(bg, -1)
                gs.aiAnimationSpeed.observe {
                    onAnimationSpeed(it)
                }
            }
        }
    }

    private fun Container.addBtn(
        bgBest: View,
        onClick: suspend () -> Unit,
        content: Container.(RoundRect) -> Unit,
    ): Container =
        container {
            val bg = roundRect(
                Size(buttonSize, buttonSize),
                uiConstants.rectCorners,
                backgroundColor,
            )
            alignTopToBottomOf(bgBest, padding / 2)
            content(bg)
            onClick {
                onClick()
            }
        }

    private fun Container.addStat(
        label: String,
        prop: ObservableProperty<Int>,
        callback: @ViewDslMarker (RoundRect.() -> Unit) = {},
    ): RoundRect {
        val bgStat = roundRect(
            Size(uiConstants.tileSize * 1.5, uiConstants.tileSize * 0.8),
            uiConstants.rectCorners,
            labelBackgroundColor,
            callback = callback
        )
        text(label, uiConstants.tileSize.toFloat() * 0.2f, labelColor, uiConstants.font) {
            centerXOn(bgStat)
            alignTopToTopOf(bgStat, statInnerPadding)
        }
        text(
            prop.value.toString(),
            uiConstants.tileSize.toFloat() * 0.35f,
            textColor,
            uiConstants.font
        ) {
            setTextBounds(
                Rectangle(
                    0.0,
                    0.0,
                    bgStat.width.toDouble(),
                    uiConstants.tileSize - statInnerPadding
                )
            )
            alignment = TextAlignment.MIDDLE_CENTER
            alignTopToTopOf(bgStat, statInnerPadding)
            centerXOn(bgStat)
            prop.observe {
                setText(it.toString())
            }
        }
        return bgStat
    }

    fun showGameOver(
        uiBoard: UiBoard,
        container: Container,
    ) = container.container {
        suspend fun handleTryAgainClick() {
            this@container.removeFromParent()
            inputManager.handleTryAgainClick()
        }
        position(uiBoard.pos)
        roundRect(uiBoard.size, uiConstants.rectCorners, Colors["#FFFFFF33"])
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

                onClick {
                    handleTryAgainClick()
                }
            }
        }
        keys.down {
            when (it.key) {
                Key.ENTER, Key.SPACE -> handleTryAgainClick()
                else                 -> Unit
            }
        }
    }

    private fun Container.addLogo(uiBoard: UiBoard): RoundRect {
        val bgLogo = roundRect(
            Size(uiConstants.tileSize, uiConstants.tileSize),
            uiConstants.rectCorners,
            accentColor,
        ) {
            alignLeftToLeftOf(uiBoard)
            positionY(30)
        }
        text(
            "2048",
            uiConstants.tileSize.toFloat() * 0.4f,
            textColor,
            uiConstants.font,
        ) {
            centerYOn(bgLogo)
            alignLeftToLeftOf(bgLogo)
        }
        return bgLogo
    }

    companion object {
        suspend operator fun invoke(injector: AsyncInjector) {
            injector.mapSingleton { StaticUi(get(), get(), get()) }
        }
    }
}
