package io.github.smaugfm.game2048.board.util

/*
 * Copyright 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import io.github.smaugfm.game2048.util.hash.Long2LongOpenHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests `LinkedHashMap`.
 */
class LinkedHashMapTest {
    @Test
    fun testAddEqualKeys() {
        val expected = Long2LongOpenHashMap()
        assertEquals(expected.size, 0)
        expected.put(45, 1)
        assertEquals(expected.size, 1)
        assertEquals(1L, expected.put(45, 1))
        assertEquals(expected.size, 1)
    }

    @Test
    fun testClear() {
        val hashMap = Long2LongOpenHashMap()
        hashMap.put(1, 1)
        assertFalse(hashMap.isEmpty)
        assertTrue(hashMap.size == 1)
        hashMap.clear()
        assertTrue(hashMap.isEmpty)
        assertTrue(hashMap.size == 0)
    }

    @Test
    fun testGet() {
        val hashMap = Long2LongOpenHashMap()
        assertEquals(Long2LongOpenHashMap.defRetValue, hashMap.get(KEY_TEST_GET))
        hashMap.put(KEY_TEST_GET, VALUE_TEST_GET)
        assertEquals(VALUE_TEST_GET, hashMap.get(KEY_TEST_GET))
    }

    @Test
    fun testPut() {
        val hashMap = Long2LongOpenHashMap()
        assertEquals(
            Long2LongOpenHashMap.defRetValue,
            hashMap.put(KEY_TEST_PUT, VALUE_TEST_PUT_1)
        )
        assertEquals(VALUE_TEST_PUT_1, hashMap.put(KEY_TEST_PUT, VALUE_TEST_PUT_2))
    }

    @Test
    fun testSize() {
        val hashMap = Long2LongOpenHashMap()

        // Test size behavior on put
        assertEquals(hashMap.size, SIZE_ZERO)
        hashMap.put(KEY_1, VALUE_1)
        assertEquals(hashMap.size, SIZE_ONE)
        hashMap.put(KEY_2, VALUE_2)
        assertEquals(hashMap.size, SIZE_TWO)
        hashMap.put(KEY_3, VALUE_3)
        assertEquals(hashMap.size, SIZE_THREE)

        // Test size behavior on clear
        hashMap.clear()
        assertEquals(hashMap.size, SIZE_ZERO)
    }

    companion object {
        private const val KEY_1 = 444L
        private const val KEY_2 = 555L
        private const val KEY_3 = 666L
        private const val KEY_TEST_CONTAINS_KEY = 1L
        private const val KEY_TEST_CONTAINS_VALUE = 2L
        private const val KEY_TEST_ENTRY_SET = "testEntrySet"
        private const val KEY_TEST_GET = 3L
        private const val KEY_TEST_KEY_SET = "testKeySet"
        private const val KEY_TEST_PUT = 234L
        private const val KEY_TEST_REMOVE = 123443L
        private const val SIZE_ONE = 1
        private const val SIZE_THREE = 3
        private const val SIZE_TWO = 2
        private const val SIZE_ZERO = 0
        private const val VALUE_1 = 222L
        private const val VALUE_2 = 444L
        private const val VALUE_3 = 555L
        private const val VALUE_TEST_CONTAINS_DOES_NOT_EXIST = 9999L
        private const val VALUE_TEST_CONTAINS_KEY = 5L
        private const val VALUE_TEST_GET = KEY_TEST_GET + 10L
        private const val VALUE_TEST_PUT_1 = KEY_TEST_PUT + 12342134L
        private const val VALUE_TEST_PUT_2 = KEY_TEST_PUT + 443344L
        private const val VALUE_TEST_REMOVE = KEY_TEST_REMOVE + 12343242L
    }
}
