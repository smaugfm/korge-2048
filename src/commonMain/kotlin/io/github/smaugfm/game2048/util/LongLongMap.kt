@file:Suppress("unused", "FunctionName")

import korlibs.datastructure.Iterator
import korlibs.datastructure.linkedHashMapOf
import kotlin.math.max

private const val a1 = 0x65d200ce55b19ad8UL
private const val b1 = 0x4f2162926e40c299UL
private const val c1 = 0x162dd799029970f8UL
private const val a2 = 0x68b665e6872bd1f4UL
private const val b2 = 0xb6cfcf9d79b51db2UL
private const val c2 = 0x7a2b92ae912898c2UL

private fun mask(value: ULong, mask: Int) =
    (value +
        ((value shr 20) and 0xFFFFFU) +
        ((value shr 40) and 0xFFFFFU)).toInt() and mask

private fun _hash1(key: Long, mask: Int): Int {
    val low = key.toULong()
    val high = (key ushr 32).toULong()

    val hash = (((a1 * low) + (b1 * high) + c1) shr 32)

    return mask(hash, mask)
}

private fun _hash2(key: Long, mask: Int): Int {
    var h = key.toULong()
    h = h xor (h shr 33)
    h *= 0xff51afd7ed558ccdUL
    h = h xor (h shr 33)
    h *= 0xc4ceb9fe1a85ec53UL
    h = h xor (h shr 33)

    return mask(h, mask)
}

private fun _hash3(input: Long, mask: Int): Int {
    var key = (input.inv()) + (input shl 21)
    key = key xor (key ushr 24)
    key += (key shl 3) + (key shl 8)
    key = key xor (key ushr 14)
    key += (key shl 2) + (key shl 4)
    key = key xor (key ushr 28)
    key += (key shl 31)

    return mask(key.toULong(), mask)
}

private fun _hash4(key: Long, mask: Int): Int {

    var x = (key xor key shr 30).toULong()
    x *= 0xbf58476d1ce4e5b9UL
    x = x xor x shr 27
    x *= 0x94d049bb133111ebUL
    x = x xor x shr 31
    return mask(x, mask)
}

private fun hash1(input: Long, mask: Int): Int = _hash1(input, mask)
private fun hash2(input: Long, mask: Int): Int = _hash2(input, mask)

private fun ilog2Ceil(v: Int): Int =
    kotlin.math.ceil(kotlin.math.log2(v.toDouble())).toInt()

class LongLongMap internal constructor(
    private var nbits: Int,
    private val loadFactor: Double,
    dummy: Boolean
) {
    constructor(initialCapacity: Int = 16, loadFactor: Double = 0.8) :
        this(max(4, ilog2Ceil(initialCapacity)), loadFactor, true)

    private var capacity = 1 shl nbits

    @PublishedApi
    internal var hasZero = false
    private var zeroValue: Long = 0
    private var mask = capacity - 1
    internal var stashSize = 1 + nbits * nbits; private set
    internal val backSize get() = capacity + stashSize

    @PublishedApi
    internal var _keys = LongArray(backSize)
    private var _values = LongArray(backSize)
    private val stashStart get() = _keys.size - stashSize
    private var growSize: Int = (capacity * loadFactor).toInt()
    var size: Int = 0; private set

    fun toMap(out: MutableMap<Long, Long> = linkedHashMapOf()): Map<Long, Long> {
        fastForEach { key, value -> out[key] = value }
        return out
    }

    private fun grow() {
        val inc = if (nbits < 20) 3 else 1
        val newnbits = nbits + inc
        val new = LongLongMap(newnbits, loadFactor, true)

        for (n in _keys.indices) {
            val k = _keys[n]
            if (k != EMPTY) new[k] = _values[n]
        }

        this.nbits = new.nbits
        this.capacity = new.capacity
        this.mask = new.mask
        this.stashSize = new.stashSize
        this._keys = new._keys
        this._values = new._values
        this.growSize = new.growSize
    }

    private fun growStash() {
        this.stashSize = this.stashSize * 2
        this._keys = this._keys.copyOf(backSize)
        this._values = this._values.copyOf(backSize)
    }

    operator fun contains(key: Long): Boolean = getKeyIndex(key) >= 0

    private fun getKeyIndex(key: Long): Int {
        if (key == 0L) return if (hasZero) ZERO_INDEX else -1
        val index1 = hash1(key, mask)
        if (_keys[index1] == key) return index1
        val index2 = hash2(key, mask)
        if (_keys[index2] == key) return index2

        for (n in stashStart until _keys.size)
            if (_keys[n] == key) return n
        return -1
    }

    fun remove(key: Long): Boolean {
        val index = getKeyIndex(key)
        if (index < 0) return false
        if (index == ZERO_INDEX) {
            hasZero = false
            zeroValue = 0
        } else {
            _keys[index] = EMPTY
        }
        size--
        return true
    }

    fun clear() {
        hasZero = false
        zeroValue = 0
        _keys.fill(0)
        _values.fill(0)
        size = 0
    }

    operator fun get(key: Long): Long {
        val index = getKeyIndex(key)
        if (index < 0) return 0
        if (index == ZERO_INDEX) return zeroValue
        return _values[index]
    }

    private fun setEmptySlot(index: Int, key: Long, value: Long): Long {
        if (_keys[index] != EMPTY) throw IllegalStateException()
        _keys[index] = key
        _values[index] = value
        size++
        return 0
    }

    operator fun set(key: Long, value: Long): Long {
        retry@ while (true) {
            val index = getKeyIndex(key)
            when {
                index < 0 -> {
                    if (key == 0L) {
                        hasZero = true
                        zeroValue = value
                        size++
                        return 0
                    }
                    if (size >= growSize) grow()
                    val index1 = hash1(key, mask)
                    if (_keys[index1] == EMPTY)
                        return setEmptySlot(
                            index1,
                            key,
                            value
                        )
                    val index2 = hash2(key, mask)
                    if (_keys[index2] == EMPTY)
                        return setEmptySlot(
                            index2,
                            key,
                            value
                        )
                    for (n in stashStart until _keys.size)
                        if (_keys[n] == EMPTY)
                            return setEmptySlot(
                                n,
                                key,
                                value
                            )
                    if (stashSize > 512) {
                        grow()
                    } else {
                        growStash()
                    }
                    continue@retry
                }

                (index == ZERO_INDEX) -> return zeroValue.apply { zeroValue = value }
                else -> return _values[index].apply { _values[index] = value }
            }
        }
    }

    inline fun getOrPut(key: Long, callback: () -> Long): Long {
        if (key !in this) set(key, callback())
        return get(key)
    }

    data class Entry(var key: Long, var value: Long)

    val keys
        get() = Iterable {
            Iterator(this).let {
                Iterator(
                    { it.hasNext() },
                    { it.nextKey() })
            }
        }
    val values
        get() = Iterable {
            Iterator(this).let {
                Iterator(
                    { it.hasNext() },
                    { it.nextValue() })
            }
        }
    val entries
        get() = Iterable {
            Iterator(this).let {
                Iterator(
                    { it.hasNext() },
                    { it.nextEntry() })
            }
        }

    class Iterator(private val map: LongLongMap) {
        private var index: Int =
            if (map.hasZero) ZERO_INDEX
            else
                nextNonEmptyIndex(map._keys, 0)
        private var entry = Entry(0, 0)

        fun hasNext() = index != EOF

        fun nextEntry(): Entry = currentEntry().apply { next() }
        fun nextKey(): Long = currentKey().apply { next() }
        fun nextValue(): Long = currentValue().apply { next() }

        private fun currentEntry(): Entry {
            entry.key = currentKey()
            entry.value = currentValue()
            return entry
        }

        private fun currentKey(): Long = when (index) {
            ZERO_INDEX, EOF -> 0
            else -> map._keys[index]
        }

        private fun currentValue(): Long = when (index) {
            ZERO_INDEX -> map.zeroValue
            EOF -> 0
            else -> map._values[index]
        }

        private fun nextNonEmptyIndex(keys: LongArray, offset: Int): Int {
            for (n in offset until keys.size) if (keys[n] != EMPTY) return n
            return EOF
        }

        private fun next() {
            if (index != EOF) index =
                nextNonEmptyIndex(map._keys, if (index == ZERO_INDEX) 0 else (index + 1))
        }
    }

    @PublishedApi
    internal fun nextNonEmptyIndex(keys: LongArray, offset: Int): Int {
        for (n in offset until keys.size) if (keys[n] != EMPTY) return n
        return EOF
    }

    inline fun fastKeyForEach(callback: (key: Long) -> Unit) {
        var index: Int = if (hasZero) ZERO_INDEX else nextNonEmptyIndex(_keys, 0)
        while (index != EOF) {
            callback(
                when (index) {
                    ZERO_INDEX, EOF -> 0
                    else -> _keys[index]
                }
            )
            index = nextNonEmptyIndex(_keys, if (index == ZERO_INDEX) 0 else (index + 1))
        }
    }

    inline fun fastValueForEach(callback: (value: Long) -> Unit) {
        fastKeyForEach { callback(this[it]) }
    }

    inline fun fastForEach(callback: (key: Long, value: Long) -> Unit) {
        fastKeyForEach { callback(it, this[it]) }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is LongLongMap) return false
        fastForEach { key, value -> if (other[key] != value) return false }
        return true
    }

    override fun hashCode(): Int {
        var out = 0
        fastForEach { key, value -> out += key.hashCode() + value.hashCode() }
        return out
    }

    companion object {
        @PublishedApi
        internal const val EOF = Int.MAX_VALUE - 1

        @PublishedApi
        internal const val ZERO_INDEX = Int.MAX_VALUE

        @PublishedApi
        internal const val EMPTY = 0L
    }

    override fun toString(): String = this.toMap().toString()
}
