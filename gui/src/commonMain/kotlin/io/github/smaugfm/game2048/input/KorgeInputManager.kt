package io.github.smaugfm.game2048.input

import io.github.smaugfm.game2048.board.Direction
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.ui.UiBoard
import korlibs.event.Key
import korlibs.inject.Injector
import korlibs.io.async.ObservableProperty
import korlibs.korge.input.SwipeDirection
import korlibs.korge.input.keys
import korlibs.korge.input.onSwipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class KorgeInputManager private constructor(
  uiBoard: UiBoard,
  isAiPlaying: ObservableProperty<Boolean>,
) : InputManager {
  private val flow = MutableSharedFlow<InputEvent>()

  init {
    uiBoard.keys {
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
          flow.emit(InputEvent.DirectionInput(dir))
        }
      }
    }
    uiBoard.onSwipe(20.0) {
      if (isAiPlaying.value)
        return@onSwipe

      when (it.direction) {
        SwipeDirection.LEFT -> Direction.LEFT
        SwipeDirection.RIGHT -> Direction.RIGHT
        SwipeDirection.TOP -> Direction.TOP
        SwipeDirection.BOTTOM -> Direction.BOTTOM
      }.let { dir ->
        flow.emit(InputEvent.DirectionInput(dir))
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
    flow.emit(InputEvent.ClickInput.AiButtonClick)
  }

  suspend fun handleAnimationSpeedClick() {
    flow.emit(InputEvent.ClickInput.AnimationSpeedClick)
  }

  override fun eventsFlow(): Flow<InputEvent> =
    flow

  companion object {

    operator fun invoke(injector: Injector) {
      injector.mapSingleton {
        KorgeInputManager(get(), get<GameState>().isAiPlaying)
      }
    }
  }
}
