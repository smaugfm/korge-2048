package io.github.smaugfm.game2048.core

@JvmInline
value class Tile(val power: Int) {
    val score get() = 1 shl power

    val isLowest get() = power == 1 || power == 2
    val isEmpty get() = power == -1
    val isNotEmpty get() = power != -1

    fun next() =
        Tile(power + 1)

    override fun toString() = power.toString()

    companion object {
        val EMPTY = Tile(-1)
        val TWO = Tile(1)
        val FOUR = Tile(2)
    }
}
