package io.github.smaugfm.game2048.util

import kotlin.math.roundToInt
import kotlin.math.sqrt

fun IntArray.toMatrixString(size: Int = sqrt(this.size.toDouble()).roundToInt()): String =
    this.joinToString("\n") { y ->
        (0 until size).map { x -> this.atXY(size, x, y) }.joinToString(", ")
    }

fun IntArray.atXY(size: Int, x: Int, y: Int): Int {
    return this[y / size + x % size]
}
