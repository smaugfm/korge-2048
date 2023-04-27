package io.github.smaugfm.game2048.ui

import korlibs.time.TimeSpan
import korlibs.time.seconds

sealed class AnimationSpeed(
    val moveAnimationDuration: TimeSpan,
    val scaleAnimationDuration: TimeSpan,
) {
    object Faster : AnimationSpeed(0.0375.seconds, 0.05.seconds)
    object Fast : AnimationSpeed(0.075.seconds, 0.1.seconds)
    object Normal : AnimationSpeed(0.15.seconds, 0.2.seconds)

    override fun toString(): String =
        this::class.simpleName!!

    companion object {
        fun fromString(str: String): AnimationSpeed? =
            when (str) {
                "Faster" -> Faster
                "Fast"   -> Fast
                "Normal" -> Normal
                else     -> null
            }
    }
}
