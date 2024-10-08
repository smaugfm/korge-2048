package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.Board4
import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.heuristics.Board4Heuristics
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.search.Search
import io.github.smaugfm.game2048.search.SearchImpl
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import io.github.smaugfm.game2048.ui.UiBoard
import korlibs.image.color.RGBA
import korlibs.inject.Injector
import korlibs.korge.Korge
import korlibs.render.GameWindow

var usingWasm: Boolean? = null

suspend fun startKorge(injector: Injector) {
    Korge(
        virtualSize = UIConstants.virtualSize,
        windowSize = UIConstants.windowSize,
        title = "2048",
        injector = injector,
        quality = GameWindow.Quality.PERFORMANCE,
        mainSceneClass = MainScene::class,
        backgroundColor = RGBA(253, 247, 240),
    ).start()
}

suspend fun createInjector(): Injector {
    val search = SearchImpl()
    search.init()
    val injector = Injector().apply {
        mapInstance(Heuristics::class, Board4Heuristics())
        mapInstance(BoardFactory::class, Board4)
        mapSingleton(Search::class) { search }
        GameState(this)
        UIConstants(this, UIConstants.Resources.load())
        History(this)
        KorgeInputManager(this)
        StaticUi(this)
        mapSingleton { UiBoard.addBoard(get(), get()) }

        mapSingleton { MainScene(get(), get(), get(), get(), get(), get(), get()) }
    }
    return injector
}
