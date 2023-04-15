package io.github.smaugfm.game2048.core

@JvmInline
value class Tile(val power: Int) {
    val score get() = 1 shl power

    val isEmpty get() = power == EMPTY_POWER
    val isNotEmpty get() = power != EMPTY_POWER

    fun next() =
        Tile(power + 1)

    override fun toString() = power.toString()

    companion object {
        private const val EMPTY_POWER = 0
        val EMPTY = Tile(EMPTY_POWER)
        val TWO = Tile(1)
        val FOUR = Tile(2)
    }
}
