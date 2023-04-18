package io.github.smaugfm.game2048.persistence

import io.github.smaugfm.game2048.ui.AnimationSpeed
import korlibs.inject.AsyncInjector
import korlibs.io.async.ObservableProperty
import korlibs.korge.service.storage.NativeStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Views

class GameState(
    private val storage: NativeStorage,
    val best: ObservableProperty<Int> = ObservableProperty(0),
    val score: ObservableProperty<Int> = ObservableProperty(0),
    val isAiPlaying: ObservableProperty<Boolean> = ObservableProperty(false),
    val animationSpeed: ObservableProperty<AnimationSpeed> = ObservableProperty(AnimationSpeed.Normal)
) {
    init {
        best.update(storage.getOrNull("best")?.toInt() ?: 0)
        score.observe {
            if (it > best.value) best.update(it)
        }
        best.observe {
            storage["best"] = it.toString()
        }
    }

    companion object {
        suspend operator fun invoke(injector: AsyncInjector) {
            injector.mapSingleton {
                GameState(injector.get<Views>().storage)
            }
        }
    }
}
