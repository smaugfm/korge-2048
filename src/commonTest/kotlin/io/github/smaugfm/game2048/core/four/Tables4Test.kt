package io.github.smaugfm.game2048.core.four

import junit.framework.TestCase.assertEquals
import org.junit.Test

class Tables4Test {
    @Test
    fun test() {
        assertEquals(0x123u.toUShort(), Tables4.reverseLine(0x3210u))
        assertEquals(0x0u.toUShort(), Tables4.reverseLine(0x0000u))
        assertEquals(0x1111u.toUShort(), Tables4.reverseLine(0x1111u))
        assertEquals(0x2211u.toUShort(), Tables4.reverseLine(0x1122u))
        assertEquals(0x2221u.toUShort(), Tables4.reverseLine(0x1222u))
    }
}
