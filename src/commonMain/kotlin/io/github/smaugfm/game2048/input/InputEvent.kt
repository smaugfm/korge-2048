package io.github.smaugfm.game2048.input

import io.github.smaugfm.game2048.board.Direction

sealed class InputEvent {
    class DirectionInput(val dir: Direction) : InputEvent()

    sealed class ClickInput : InputEvent() {
        object RestartClick : ClickInput()
        object UndoClick : ClickInput()
        object AiToggleClick : ClickInput()
        object TryAgainClick : ClickInput()
    }
}
