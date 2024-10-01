package io.github.smaugfm.game2048.util

class CircularAppendOnlyList<T>(
    maxSize: Int,
) : Iterable<T> {
    private val arr = MutableList<T?>(maxSize) { null }
    private var end = 0

    var size = 0
        private set

    companion object {
        private val emptyIterator = object : Iterator<Any> {
            override fun hasNext(): Boolean = false

            override fun next(): Any {
                throw NoSuchElementException()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        if (isEmpty) return emptyIterator as Iterator<T>

        return object : Iterator<T> {
            private var iterated = 0
            private var cur = end

            override fun hasNext(): Boolean =
                iterated < size

            private fun advance() {
                cur = (cur + 1) % arr.size
            }

            override fun next(): T {
                if (!hasNext())
                    throw NoSuchElementException()
                while (arr[cur] == null) {
                    advance()
                }
                return arr[cur]!!.also {
                    iterated++
                    advance()
                }
            }
        }
    }

    fun addLastAll(vararg items: T) {
        for (i in items)
            addLast(i)
    }

    fun addLastAll(items: Iterable<T>) {
        for (i in items)
            addLast(i)
    }

    fun addLast(item: T) {
        arr[end++] = item
        end %= arr.size
        if (!isFull) {
            size++
        }
    }

    fun clear() {
        arr.fill(null)
        size = 0
        end = 0
    }

    private fun removeLastOrNull(): T? {
        if (isEmpty) return null
        if (--end < 0)
            end = arr.lastIndex
        return arr[end].also {
            arr[end] = null
            size--
        }
    }

    fun removeLast(): T =
        removeLastOrNull()
            ?: throw NoSuchElementException("Circular queue is empty")

    val isEmpty get() = size == 0
    val isFull get() = size == arr.size

}
