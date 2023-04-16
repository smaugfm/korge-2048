package io.github.smaugfm.game2048.input

import io.github.smaugfm.game2048.board.Direction

sealed class InputEvent {
    sealed class DirectionInput(val dir: Direction) : InputEvent() {
        class UserDirection(dir: Direction) : DirectionInput(dir)
        class AiDirection(dir: Direction) : DirectionInput(dir)
    }

    sealed class ClickInput : InputEvent() {
        object RestartClick : ClickInput()
        object UndoClick : ClickInput()
        object AiToggleClick : ClickInput()
        object TryAgainClick : ClickInput()
    }
}
