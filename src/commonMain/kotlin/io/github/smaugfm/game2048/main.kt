package io.github.smaugfm.game2048

import LongLongMap
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
import korlibs.datastructure.IntIntMap
import korlibs.datastructure.random.FastRandom
import korlibs.image.color.RGBA
import korlibs.inject.AsyncInjector
import korlibs.korge.Korge
import korlibs.korge.KorgeConfig
import korlibs.math.geom.Size

const val boardSize = 4
const val boardArraySize = boardSize * boardSize

suspend fun main() {
    val searchCache = LongLongMap()

    val injector = AsyncInjector().apply {
//        mapInstance(Heuristics::class, NneonneoAnySizeHeuristics())
//        mapInstance(BoardFactory::class, AnySizeBoard.Companion)
//        mapSingleton(Expectimax::class) { AnySizeExpectimax(get()) }
//        mapSingleton { MainScene<AnySizeBoard>() }

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
            windowSize = Size(480, 640),
            title = "2048",
            injector = injector,
            mainSceneClass = MainScene::class,
            backgroundColor = RGBA(253, 247, 240),
        )
    ) {
    }
}
