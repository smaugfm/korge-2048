package io.github.smaugfm.game2048.persistence

import io.github.smaugfm.game2048.boardArraySize
import io.github.smaugfm.game2048.board.Tile
import korlibs.datastructure.iterators.fastForEach

class History(from: String?, private val onUpdate: (History) -> Unit) {
    class Element(val powers: Array<Tile>, val score: Int)

    private val history = mutableListOf<Element>()

    val currentElement: Element
        get() = history.last()

    init {
        from
            .takeUnless { it?.isBlank() == true }
            ?.split(';')
            ?.fastForEach {
                val element = elementFromString(it)
                history.add(element)
            }
    }

    fun add(powers: Array<Tile>, score: Int) {
        history.add(Element(powers, score))
        onUpdate(this)
    }

    fun undo(): Element {
        if (history.size > 1) {
            history.removeAt(history.size - 1)
            onUpdate(this)
        }
        return history.last()
    }

    fun clear() {
        history.clear()
        onUpdate(this)
    }

    fun isEmpty() = history.isEmpty()

    private fun elementFromString(string: String): Element {
        val powers = string.split(',').map { it.toInt() }
        if (powers.size != boardArraySize + 1) throw IllegalArgumentException("Incorrect history")
        return Element(Array(boardArraySize) { Tile(powers[it]) }, powers[boardArraySize])
    }

    override fun toString(): String {
        return history.joinToString(";") {
            it.powers.joinToString(",") + "," + it.score
        }
    }
}
