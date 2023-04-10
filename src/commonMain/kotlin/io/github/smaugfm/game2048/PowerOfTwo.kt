package io.github.smaugfm.game2048

@JvmInline
value class PowerOfTwo(val power: Int) {
    val score get() = 1 shl power
    val isLowest get() = power == 1 || power == 2
    fun next() =
        PowerOfTwo(power + 1)
}
