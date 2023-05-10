package io.github.smaugfm.game2048.persistence

import io.github.smaugfm.game2048.board.Tile
import io.github.smaugfm.game2048.board.boardArraySize
import io.github.smaugfm.game2048.util.CircularAppendOnlyList
import korlibs.inject.AsyncInjector
import korlibs.korge.service.storage.storage
import korlibs.korge.view.Views

class History private constructor(
    from: String?,
    val maxSize: Int = 512,
    private val onUpdate: (History) -> Unit,
) {
    class Element(val tiles: Array<Tile>, val score: Int)

    private val history = CircularAppendOnlyList<Element>(maxSize)

    val currentElement: Element
        get() = history.last()

    init {
        from
            .takeUnless { it?.isBlank() == true }
            ?.split(';')
            ?.takeLast(maxSize)
            ?.map(::elementFromString)
            ?.let {
                history.addLastAll(it)
            }
    }

    fun add(tiles: Array<Tile>, score: Int) {
        history.addLast(Element(tiles, score))
        onUpdate(this)
    }

    fun undo(): Element {
        if (history.size > 1) {
            history.removeLast()
            onUpdate(this)
        }
        return history.last()
    }

    fun clear() {
        history.clear()
        onUpdate(this)
    }

    fun isEmpty() = history.isEmpty

    private fun elementFromString(string: String): Element {
        val tiles = string.split(',').map { it.toInt() }
        if (tiles.size != boardArraySize + 1) throw IllegalArgumentException("Incorrect history")
        return Element(Array(boardArraySize) { Tile(tiles[it]) }, tiles[boardArraySize])
    }

    override fun toString(): String {
        return history.joinToString(";") {
            it.tiles.joinToString(",") + "," + it.score
        }
    }

    companion object {
        suspend operator fun invoke(injector: AsyncInjector) {
            injector.mapSingleton {
                val views: Views = get()
                History(views.storage.getOrNull("history")) {
                    views.storage["history"] = it.toString()
                }
            }
        }
    }
}
