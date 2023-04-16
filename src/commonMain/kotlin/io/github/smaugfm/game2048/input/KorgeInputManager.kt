package io.github.smaugfm.game2048.input

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.isAiPlaying
import korlibs.event.Key
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.keys
import korlibs.korge.input.onSwipe
import korlibs.korge.view.Stage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class KorgeInputManager(stage: Stage) : InputManager {
    private val flow = MutableSharedFlow<InputEvent>()

    init {
        stage.keys {
            down { keyEvent ->
                if (isAiPlaying.value)
                    return@down

                when (keyEvent.key) {
                    Key.LEFT -> Direction.LEFT
                    Key.RIGHT -> Direction.RIGHT
                    Key.UP -> Direction.TOP
                    Key.DOWN -> Direction.BOTTOM
                    else -> null
                }?.let { dir ->
                    flow.emit(InputEvent.DirectionInput.UserDirection(dir))
                }
            }
        }
        stage.onSwipe(20.0) {
            if (isAiPlaying.value)
                return@onSwipe
            when (it.direction) {
                SwipeDirection.LEFT -> Direction.LEFT
                SwipeDirection.RIGHT -> Direction.RIGHT
                SwipeDirection.TOP -> Direction.TOP
                SwipeDirection.BOTTOM -> Direction.BOTTOM
            }.let {
                flow.emit(InputEvent.DirectionInput.AiDirection(it))
            }
        }
    }

    suspend fun handleRestartClick() {
        flow.emit(InputEvent.ClickInput.RestartClick)
    }

    suspend fun handleUndoClick() {
        flow.emit(InputEvent.ClickInput.UndoClick)
    }

    suspend fun handleTryAgainClick() {
        flow.emit(InputEvent.ClickInput.TryAgainClick)
    }

    suspend fun handleAiClick() {
        flow.emit(InputEvent.ClickInput.AiToggleClick)
    }

    override fun eventsFlow(): Flow<InputEvent> =
        flow
}
