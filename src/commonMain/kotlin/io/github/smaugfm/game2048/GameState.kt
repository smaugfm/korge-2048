package io.github.smaugfm.game2048

import korlibs.io.async.ObservableProperty

data class GameState(
    val best: ObservableProperty<Int> = ObservableProperty(0),
    val score: ObservableProperty<Int> = ObservableProperty(0),
    val isAiPlaying: ObservableProperty<Boolean> = ObservableProperty(false)
)
