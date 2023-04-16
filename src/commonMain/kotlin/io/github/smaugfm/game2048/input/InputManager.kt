package io.github.smaugfm.game2048.input

import kotlinx.coroutines.flow.Flow

interface InputManager {
    fun eventsFlow(): Flow<InputEvent>
}
