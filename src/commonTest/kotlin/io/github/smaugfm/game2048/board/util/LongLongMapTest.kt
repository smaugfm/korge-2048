import korlibs.datastructure.IntIntMap
import kotlin.test.Test
import kotlin.test.assertEquals

class LongLongMapTest {
    @Test
    fun load() {
        val m = LongLongMap()
        val max = 524_287L * 2
        for (n in 0 until max)
            m[n] = n * 10L
        for (n in 0 until max) {
            assertEquals(n * 10L, m[n])
            assertEquals(true, m.contains(n))
        }
        assertEquals(0L, m[-1])
        assertEquals(0L, m[max + 1])
        assertEquals(false, m.contains(-1))
        assertEquals(false, m.contains(max + 1))
    }

    @Test
    fun load2() {
        val m = IntIntMap()
        val max = 524_287 * 8
        for (n in 0 until max)
            m[n] = n * 10
        for (n in 0 until max) {
            assertEquals(n * 10, m[n])
            assertEquals(true, m.contains(n))
        }
        assertEquals(0, m[-1])
        assertEquals(0, m[max + 1])
        assertEquals(false, m.contains(-1))
        assertEquals(false, m.contains(max + 1))
    }

    @Test
    fun simple() {
        val m = LongLongMap()
        assertEquals(0, m.size)
        assertEquals(0, m[0])

        m[0] = 98
        assertEquals(1, m.size)
        assertEquals(98, m[0])
        assertEquals(0, m[1])

        m[0] = 99
        assertEquals(1, m.size)
        assertEquals(99, m[0])
        assertEquals(0, m[1])

        m.remove(0)
        assertEquals(0, m.size)
        assertEquals(0, m[0])
        assertEquals(0, m[1])

        m.remove(0)
    }

    @Test
    fun name2() {
        val m = LongLongMap()
        for (n in 0 until 1000L)
            m[n] = n * 1000L
        for (n in 0 until 1000L) {
            assertEquals(n * 1000, m[n])
            assertEquals(true, m.contains(n))
        }
        assertEquals(0, m[-1])
        assertEquals(0, m[1001])
        assertEquals(false, m.contains(-1))
        assertEquals(false, m.contains(1001))
    }
}
