package io.github.smaugfm.game2048.board.util

import io.github.smaugfm.game2048.util.CircularAppendOnlyList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CircularQueueTest {
    @Test
    fun test() {
        val queue = CircularAppendOnlyList<Int>(3)
        assertFailsWith<NoSuchElementException> {
            queue.removeLast()
        }

        assertEquals(0, queue.size)
        queue.addLast(1)
        queue.addLast(2)
        assertEquals(2, queue.size)

        assertEquals(2, queue.removeLast())
        assertEquals(1, queue.size)

        queue.addLast(3)
        assertEquals(2, queue.size)

        queue.addLast(4)
        assertEquals(listOf(1, 3, 4), queue.toList())

        queue.addLast(5)
        assertEquals(listOf(3, 4, 5), queue.toList())

        assertEquals(5, queue.removeLast())
        assertEquals(listOf(3, 4), queue.toList())

        assertEquals(4, queue.removeLast())
        assertEquals(listOf(3), queue.toList())

        assertEquals(3, queue.removeLast())
        assertEquals(0, queue.size)
        assertEquals(emptyList(), queue.toList())

        assertFailsWith<NoSuchElementException> {
            queue.removeLast()
        }
        assertEquals(0, queue.size)

        queue.addLastAll(1, 2, 3)
        queue.addLastAll(1, 2, 3)
        assertEquals(listOf(1, 2, 3), queue.toList())
        assertEquals(3, queue.size)

        queue.addLastAll(4, 5, 6)
        assertEquals(listOf(4, 5, 6), queue.toList())
        assertEquals(3, queue.size)

        queue.clear()
        assertEquals(emptyList(), queue.toList())
        assertEquals(0, queue.size)
    }
}
