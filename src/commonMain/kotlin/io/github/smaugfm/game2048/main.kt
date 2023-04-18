package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.expectimax.impl.Board4Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import korlibs.image.color.RGBA
import korlibs.inject.AsyncInjector
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.math.geom.Size
import korlibs.render.GameWindow

const val boardSize = 4
const val boardArraySize = boardSize * boardSize

suspend fun main() {
    val injector = AsyncInjector().apply {
        mapInstance(Heuristics::class, Board4Heuristics())
        mapInstance(BoardFactory::class, Board4.Companion)
        mapSingleton(Expectimax::class) { Board4Expectimax(get()) }
        mapSingleton { MainScene<Board4>() }

        mapInstance(GameState())
        UIConstants(this)
        History(this)
        KorgeInputManager(this)
        StaticUi(this)
    }
    Korge(
        KorgeConfig(
            virtualSize = UIConstants.virtualSize,
            windowSize = UIConstants.windowSize,
            title = "2048",
            injector = injector,
            quality = GameWindow.Quality.QUALITY,
            mainSceneClass = MainScene::class,
            backgroundColor = RGBA(253, 247, 240),
        )
    ) {
    }
}
