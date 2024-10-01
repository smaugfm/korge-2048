package io.github.smaugfm.game2048.ui

import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.ui.UIConstants.Companion.accentColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.backgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.gameOverTextColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelBackgroundColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.labelColor
import io.github.smaugfm.game2048.ui.UIConstants.Companion.textColor
import io.github.smaugfm.game2048.usingWasm
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.text.TextAlignment
import korlibs.inject.Injector
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.*
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
import korlibs.korge.view.positionY
import korlibs.korge.view.roundRect
import korlibs.korge.view.setText
import korlibs.korge.view.size
import korlibs.korge.view.text
import korlibs.math.geom.Rectangle
import korlibs.math.geom.Size
import korlibs.util.format
import kotlin.math.roundToInt

class StaticUi(
    private val gs: GameState,
    private val inputManager: KorgeInputManager,
    private val uiConstants: UIConstants,
) {
    private val buttonSize: Double = uiConstants.buttonSize
    private val underBoardLabelSize = uiConstants.tileSize * 0.3 * 0.5
    private val padding = uiConstants.padding
    private val smallPadding = uiConstants.smallPadding
    private val betweenButtonsPadding = padding / 1.5
    private val statMargin = uiConstants.padding
    private val statInnerPadding = uiConstants.tileSize / 8

    suspend fun addStaticUi(
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
        val depthText = addUnderBoardLabel(gs.aiDepth, {
            if (gs.showAiStats.value) {
                "depth %d".format(it)
            } else ""
        }) {
            alignRightToRightOf(uiBoard)
            alignTopToBottomOf(uiBoard, smallPadding)
        }
        addUnderBoardLabel(gs.aiElapsedMs, {
            if (gs.showAiStats.value) {
                if (it > 1)
                    "%dms".format(it.roundToInt())
                else
                    "0ms"
            } else
                ""
        }) {
            alignRightToLeftOf(depthText, smallPadding)
            alignTopToBottomOf(uiBoard, smallPadding)
        }

        addUnderBoardLabel(
            gs.aiElapsedMs,
            {
                if (usingWasm && gs.showAiStats.value) "wasm" else ""
            }
        ) {
            alignRightToRightOf(uiBoard)
            alignTopToBottomOf(uiBoard, smallPadding * 2 + underBoardLabelSize)
        }
    }

    private fun Container.addMoveLabel(uiBoard: UiBoard) {
        addUnderBoardLabel(
            gs.moveNumber,
            { "move #%d".format(it) }
        ) {
            alignLeftToLeftOf(uiBoard)
            alignTopToBottomOf(uiBoard, smallPadding)
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
            underBoardLabelSize.toFloat(),
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

    private suspend fun Container.addButtons(
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
            alignRightToLeftOf(restartBlock, betweenButtonsPadding)
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
            alignRightToLeftOf(undoBlock, betweenButtonsPadding)
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
                    AnimationSpeed.Normal -> "x1"
                    AnimationSpeed.Fast -> "x2"
                    AnimationSpeed.NoAnimation -> "x3"
                }
            }

            this.visible = gs.isAiPlaying.value
            gs.isAiPlaying.observe {
                this.visible = it
            }
            alignRightToLeftOf(aiBlock, betweenButtonsPadding)
            text(
                "AI",
                (buttonSize * 0.7).toFloat(),
                textColor,
                uiConstants.fontBold
            ) {
                onAnimationSpeed(gs.animationSpeed)
                centerXOn(bg)
                alignTopToTopOf(bg, -1)
                gs.observeAnimationSpeed {
                    onAnimationSpeed(it)
                }
                gs.isAiPlaying.observe {
                    onAnimationSpeed(gs.animationSpeed)
                }
            }
        }
    }

    private suspend fun Container.addBtn(
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
            alignTopToBottomOf(bgBest, padding)
            content(bg)
            onClickSuspend {
                onClick()
            }
        }

    private fun Container.addStat(
        label: String,
        prop: ObservableProperty<Int>,
        callback: @ViewDslMarker (RoundRect.() -> Unit) = {},
    ): RoundRect {
        val bgStat = roundRect(
            Size(uiConstants.tileSize * 1.5, uiConstants.statHeight),
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
                    bgStat.width,
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

    suspend fun showGameOver(uiBoard: UiBoard) =
        uiBoard.container {
            suspend fun handleTryAgainClick() {
                this@container.removeFromParent()
                inputManager.handleTryAgainClick()
            }
            roundRect(uiBoard.size, uiConstants.rectCorners, Colors["#FFFFFFBB"])
            text("Game Over", 90f, gameOverTextColor, uiConstants.font) {
                centerOn(uiBoard)
                y -= 50
            }
            val tryAgainText =
                Text(
                    "Try again",
                    60f,
                    textColor,
                    uiConstants.font,
                )
            val bg = Colors["#000000AA"]
            val hover = Colors["#464646AA"]
            val pressed = Colors["#646464AA"]
            val tryAgainBg = roundRect(
                Size(
                    tryAgainText.width + padding,
                    tryAgainText.height + smallPadding,
                ),
                uiConstants.rectCorners,
            ) {
                centerOn(uiBoard)
                y += 70

                color = bg
                onOverSuspend {
                    onOver { color = hover }
                    onOut { color = bg }
                    onDown { color = pressed }
                    onUp { color = pressed }

                    onClickSuspend {
                        handleTryAgainClick()
                    }
                }
            }
            addChild(tryAgainText)
            tryAgainText.centerOn(tryAgainBg)
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
            positionY(padding)
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
        operator fun invoke(injector: Injector) {
            injector.mapSingleton { StaticUi(get(), get(), get()) }
        }
    }
}
