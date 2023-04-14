package io.github.smaugfm.game2048.core

@JvmInline
value class Board4(val packed: ULong) : Board {
    constructor(array: IntArray) : this(fromArray(array))

    fun transpose(): Board4 {
        val a1 = packed and 0xf0f00f0ff0f00f0fUL
        val a2 = packed and 0x0000f0f00000f0f0UL
        val a3 = packed and 0x0f0f00000f0f0000UL
        val a = a1 or (a2 shl 12) or (a3 shr 12);
        val b1 = packed and 0xff00ff0000ff00ffUL
        val b2 = packed and 0x00ff00ff00000000UL
        val b3 = packed and 0x00000000ff00ff00UL
        return Board4(b1 or (b2 shr 24) or (b3 shl 24))
    }

    fun countEmptyTiles(): Int {
        assert(packed != 0UL)

        var x = packed
        x = x or ((x shr 2) and 0x3333333333333333UL)
        x = x or (x shr 1)
        x = x.inv() and 0x1111111111111111UL
        x += x shr 32
        x += x shr 16
        x += x shr 8
        x += x shr 4
        return (x and 0xFUL).toInt()
    }


    companion object {
        private fun fromArray(array: IntArray): ULong {
            var result = 0UL
            array.forEachIndexed { index, value ->
                if (value > 0)
                    result = result or ((value and 0xF).toULong() shl (index * 4))
            }

            return result
        }
    }
}
