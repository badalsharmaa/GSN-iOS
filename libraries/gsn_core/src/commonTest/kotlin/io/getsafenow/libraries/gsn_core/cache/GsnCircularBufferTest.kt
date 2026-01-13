package io.getsafenow.libraries.gsn_core.cache

import io.getsafenow.libraries.gsn_core.helpers.GsnCircularBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GsnCircularBufferTest {
    @Test
    fun `when putting more than cache size then cache is limited to cache size`() {
        val (cache, internalData) = createIntCache(cacheSize = 3)

        cache.putInOrder(1, 1, 1, 1, 1, 1)

        assertEquals(listOf(1, 1, 1), internalData.toList())
    }

    @Test
    fun `when putting more than cache then acts as FIFO`() {
        val (cache, internalData) = createIntCache(cacheSize = 3)

        cache.putInOrder(1, 2, 3, 4)

        assertEquals(listOf(4, 2, 3), internalData.toList())
    }

    @Test
    fun `given empty cache when checking if contains key then is false`() {
        val (cache, _) = createIntCache(cacheSize = 3)

        val result = cache.contains(1)

        assertFalse(result)
    }

    @Test
    fun `given cached key when checking if contains key then is true`() {
        val (cache, _) = createIntCache(cacheSize = 3)

        cache.add(1)
        val result = cache.contains(1)

        assertTrue(result)
    }

    private fun createIntCache(cacheSize: Int): Pair<GsnCircularBuffer<Int>, Array<Int?>> {
        var internalData: Array<Int?>? = null
        val factory: (Int) -> Array<Int?> = {
            Array<Int?>(it) { null }.also { array -> internalData = array }
        }
        return GsnCircularBuffer(cacheSize, factory) to internalData!!
    }

    private fun GsnCircularBuffer<Int>.putInOrder(vararg values: Int) {
        values.forEach { add(it) }
    }
}