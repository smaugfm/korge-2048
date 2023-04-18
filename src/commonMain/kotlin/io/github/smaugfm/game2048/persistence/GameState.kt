package io.github.smaugfm.game2048.persistence

import io.github.smaugfm.game2048.ui.AnimationSpeed
import korlibs.inject.AsyncInjector
import korlibs.io.async.ObservableProperty
import korlibs.korge.service.storage.NativeStorage
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Views

class GameState(
    private val storage: NativeStorage,
    val best: ObservableProperty<Int>,
    val score: ObservableProperty<Int>,
    val isAiPlaying: ObservableProperty<Boolean>,
    val moveNumber: ObservableProperty<Int>,
    val animationSpeed: ObservableProperty<AnimationSpeed> =
        ObservableProperty(AnimationSpeed.Normal)
) {
    init {
        score.observe {
            if (it > best.value) best.update(it)
        }
        best.observe {
            storage["best"] = it.toString()
        }
        moveNumber.observe {
            storage["moveNumber"] = it.toString()
        }
        animationSpeed.observe {
            storage["animationSpeed"] = it.toString()
        }
    }

    companion object {
        suspend operator fun invoke(injector: AsyncInjector) {
            injector.mapSingleton {
                val storage = injector.get<Views>().storage
                GameState(
                    storage,
                    ObservableProperty(storage.getOrNull("best")?.toInt() ?: 0),
                    ObservableProperty(0),
                    ObservableProperty(false),
                    ObservableProperty(storage.getOrNull("moveNumber")?.toInt() ?: 0),
                    ObservableProperty(
                        storage.getOrNull("animationSpeed")
                            ?.let(AnimationSpeed::fromString)
                            ?: AnimationSpeed.Normal
                    )
                )
            }
        }
    }
}
