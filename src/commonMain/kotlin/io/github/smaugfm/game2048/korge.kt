package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.FindBestMove
import io.github.smaugfm.game2048.expectimax.FindBestMoveImpl
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import korlibs.image.color.RGBA
import korlibs.inject.AsyncInjector
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.render.GameWindow

suspend fun startKorge(injector: AsyncInjector) {
    Korge(
        KorgeConfig(
            virtualSize = UIConstants.virtualSize,
            windowSize = UIConstants.windowSize,
            title = "2048",
            injector = injector,
            quality = GameWindow.Quality.PERFORMANCE,
            mainSceneClass = MainScene::class,
            backgroundColor = RGBA(253, 247, 240),
        )
    ) {
    }
}

suspend fun createInjector(): AsyncInjector {
    val injector = AsyncInjector().apply {
        mapInstance(Heuristics::class, Board4Heuristics())
        mapInstance(BoardFactory::class, Board4)
        mapSingleton(FindBestMove::class) { FindBestMoveImpl() }
        GameState(this)
        UIConstants(this)
        History(this)
        KorgeInputManager(this)
        StaticUi(this)

        mapSingleton { MainScene(get(), get(), get(), get(), get(), get()) }
    }
    return injector
}
