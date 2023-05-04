package io.github.smaugfm.game2048.util.hash

import kotlin.math.ceil

object HashCommon {
    fun nextPowerOfTwo(x: Long): Long {
        var x = x
        if (x == 0L) return 1
        x--
        x = x or (x shr 1)
        x = x or (x shr 2)
        x = x or (x shr 4)
        x = x or (x shr 8)
        x = x or (x shr 16)
        return (x or (x shr 32)) + 1
    }

    fun maxFill(n: Int, f: Float): Int {
        return ceil((n * f).toDouble()).toInt()
    }

    fun arraySize(expected: Int, f: Float): Int {
        val s = nextPowerOfTwo(ceil((expected / f).toDouble()).toLong())
            .toLong()
        if (s > 1 shl 30) throw IllegalArgumentException("Too large ($expected expected elements with load factor $f)")
        return s.toInt()
    }
}
