package io.github.smaugfm.game2048

import io.github.smaugfm.game2048.board.BoardFactory
import io.github.smaugfm.game2048.board.impl.Board4
import io.github.smaugfm.game2048.expectimax.Expectimax
import io.github.smaugfm.game2048.heuristics.Heuristics
import io.github.smaugfm.game2048.heuristics.impl.Board4Heuristics
import io.github.smaugfm.game2048.input.KorgeInputManager
import io.github.smaugfm.game2048.persistence.GameState
import io.github.smaugfm.game2048.persistence.History
import io.github.smaugfm.game2048.transposition.ConcurrentHashMapTranspositionTable
import io.github.smaugfm.game2048.transposition.TranspositionTable
import io.github.smaugfm.game2048.ui.StaticUi
import io.github.smaugfm.game2048.ui.UIConstants
import korlibs.image.color.RGBA
import korlibs.inject.AsyncInjector
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.render.GameWindow

const val boardSize = 4
const val boardArraySize = boardSize * boardSize

suspend fun main() {
    val injector = AsyncInjector().apply {
        mapInstance(TranspositionTable::class, ConcurrentHashMapTranspositionTable())
        mapInstance(Heuristics::class, Board4Heuristics())
        mapInstance(BoardFactory::class, Board4.Companion)
        mapSingleton(Expectimax::class) { Expectimax.create(get(), get()) }
        GameState(this)
        UIConstants(this)
        History(this)
        KorgeInputManager(this)
        StaticUi(this)

        mapSingleton { MainScene(get(), get(), get(), get(), get(), get()) }
    }
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
