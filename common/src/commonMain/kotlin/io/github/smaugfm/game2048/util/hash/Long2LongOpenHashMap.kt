package io.github.smaugfm.game2048.util.hash

import io.github.smaugfm.game2048.util.hash.HashCommon.arraySize
import io.github.smaugfm.game2048.util.hash.HashCommon.maxFill

class Long2LongOpenHashMap(
    expected: Int = DEFAULT_INITIAL_SIZE,
    f: Float = DEFAULT_LOAD_FACTOR,
) {
    private var key: LongArray
    private var value: LongArray
    private var used: BooleanArray
    private val f: Float
    private var n = 0
    private var maxFill = 0
    private var mask = 0

    var size = 0
        private set

    init {
        if (f <= 0 || f > 1) throw IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1")
        if (expected < 0) throw IllegalArgumentException("The expected number of elements must be nonnegative")
        this.f = f
        n = arraySize(expected, f)
        mask = n - 1
        maxFill = maxFill(n, f)
        key = LongArray(n)
        value = LongArray(n)
        used = BooleanArray(n)
    }

    /*
	 * The following methods implements some basic building blocks used by
	 * all accessors. They are (and should be maintained) identical to those used in OpenHashSet.drv.
	 */
    fun put(k: Long, v: Long): Long {
        // The starting point.
        var pos = murmurHash3(k).toInt() and mask
        // There's always an unused entry.
        while (used[pos]) {
            if (key[pos] == k) {
                val oldValue = value[pos]
                value[pos] = v
                return oldValue
            }
            pos = pos + 1 and mask
        }
        used[pos] = true
        key[pos] = k
        value[pos] = v
        if (++size >= maxFill) rehash(arraySize(size + 1, f))
        return defRetValue
    }

    operator fun get(k: Long): Long {
        // The starting point.
        var pos = murmurHash3(k).toInt() and mask
        // There's always an unused entry.
        while (used[pos]) {
            if (key[pos] == k) return value[pos]
            pos = pos + 1 and mask
        }
        return defRetValue
    }

    fun clear() {
        if (size == 0) return
        size = 0
        fill(used, false)
    }

    val isEmpty: Boolean
        get() = size == 0

    private fun rehash(newN: Int) {
        var i = 0
        var pos: Int
        val used = used
        var k: Long
        val key = key
        val value = value
        val newMask = newN - 1
        val newKey = LongArray(newN)
        val newValue = LongArray(newN)
        val newUsed = BooleanArray(newN)
        var j = size
        while (j-- != 0) {
            while (!used[i]) i++
            k = key[i]
            pos = murmurHash3(k).toInt() and newMask
            while (newUsed[pos]) pos = pos + 1 and newMask
            newUsed[pos] = true
            newKey[pos] = k
            newValue[pos] = value[i]
            i++
        }
        n = newN
        mask = newMask
        maxFill = maxFill(n, f)
        this.key = newKey
        this.value = newValue
        this.used = newUsed
    }

    companion object {
        const val DEFAULT_INITIAL_SIZE = 16
        const val DEFAULT_LOAD_FACTOR = .75f
        const val defRetValue = 0L

        private fun fill(array: BooleanArray, value: Boolean) {
            var i = array.size
            while (i-- != 0) array[i] = value
        }

        fun murmurHash3(num: Long): Long {
            var x = num
            x = x xor (x ushr 33)
            x *= -0xae502812aa7333L
            x = x xor (x ushr 33)
            x *= -0x3b314601e57a13adL
            x = x xor (x ushr 33)
            return x
        }
    }
}
