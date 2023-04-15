package io.github.smaugfm.game2048.core

enum class Direction {
    LEFT, RIGHT, TOP, BOTTOM;

    companion object {
        val directions = Direction.values().toList().toTypedArray()
    }
}
