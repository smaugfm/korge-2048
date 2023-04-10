import korlibs.datastructure.*
import kotlin.random.*

class PositionMap(
    private val array: IntArray2 = IntArray2(4, 4, -1)
) {
    fun copy() =
        PositionMap(array.copy(data = array.data.copyOf()))

    fun getNotEmptyPositionFrom(direction: Direction, line: Int): Position? {
        when (direction) {
            Direction.LEFT -> for (i in 0..3) getOrNull(i, line)?.let { return it }
            Direction.RIGHT -> for (i in 3 downTo 0) getOrNull(i, line)?.let { return it }
            Direction.TOP -> for (i in 0..3) getOrNull(line, i)?.let { return it }
            Direction.BOTTOM -> for (i in 3 downTo 0) getOrNull(line, i)?.let { return it }
        }
        return null
    }

    private fun getOrNull(x: Int, y: Int) =
        if (array[x, y] != -1)
            Position(x, y)
        else
            null

    private fun getNumber(x: Int, y: Int) =
        array.tryGet(x, y)?.let {
            blocks[it]?.number?.ordinal ?: -1
        } ?: -1

    operator fun get(x: Int, y: Int) = array[x, y]

    operator fun set(x: Int, y: Int, value: Int) {
        array[x, y] = value
    }

    fun forEach(action: (Int) -> Unit) {
        array.forEach(action)
    }

    fun getRandomFreePosition(): Position? {
        val quantity = array.count { it == -1 }
        if (quantity == 0) return null
        val chosen = Random.nextInt(quantity)
        var current = -1
        array.each { x, y, value ->
            if (value == -1) {
                current++
                if (current == chosen) {
                    return Position(x, y)
                }
            }
        }
        return null
    }

    override fun equals(other: Any?): Boolean =
        (other is PositionMap) && this.array.data.contentEquals(other.array.data)

    override fun hashCode() = array.hashCode()
    fun hasAvailableMoves(): Boolean =
        array.any { x, y, _ -> hasAdjacentEqualPosition(x, y) }

    private fun hasAdjacentEqualPosition(x: Int, y: Int) =
        getNumber(x, y)
            .let {
                it == getNumber(x - 1, y) ||
                    it == getNumber(x + 1, y) ||
                    it == getNumber(x, y - 1) ||
                    it == getNumber(x, y + 1)
            }

    fun toNumberIds() = IntArray(16) { getNumber(it % 4, it / 4) }
}

inline fun IntArray2.any(gen: (x: Int, y: Int, v: Int) -> Boolean): Boolean {
    each { x, y, v ->
        if (gen(x, y, v)) return true
    }
    return false
}
