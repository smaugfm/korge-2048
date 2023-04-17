package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import korlibs.image.color.RGBA
import korlibs.inject.AsyncInjector
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.math.geom.Size

const val boardSize = 4
const val boardArraySize = boardSize * boardSize

suspend fun main() {
    val injector = AsyncInjector().apply {
        mapInstance(GameState())
        UIConstants(this)
        History(this)
        KorgeInputManager(this)
        StaticUi(this)
        MainScene(this)
    }
    Korge(
        KorgeConfig(
            windowSize = Size(480, 640),
            title = "2048",
            injector = injector,
            mainSceneClass = MainScene::class,
            backgroundColor = RGBA(253, 247, 240),
        )
    ) {
    }
}
