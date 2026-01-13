package io.getsafenow.libraries.gsn_core.extensionshelper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class BasicKmpExtensionsTest {

    // ---------- Boolean extensions ----------

    @Test
    fun testBooleanToOnOff() {
        assertEquals("ON", true.toOnOff())
        assertEquals("OFF", false.toOnOff())
    }

    @Test
    fun testBooleanTo01() {
        assertEquals("1", true.to01())
        assertEquals("0", false.to01())
    }

    // ---------- Generic extension ----------

    @Test
    fun testOoi() {
        var called = false
        val result = "value".ooi { called = true }
        assertEquals("value", result)
        assertTrue(called)
    }

    @Test
    fun testOrEmpty() {
        val nonNull: CharSequence? = "hello"
        val nullValue: CharSequence? = null
        assertEquals("hello", nonNull.orEmpty())
        assertEquals("", nullValue.orEmpty())
    }

    // ---------- String manipulation ----------

    @Test
    fun testInsertBeforeLast() {
        assertEquals("file_foo.txt", "file.txt".insertBeforeLast("_foo"))
        assertEquals("file_foo", "file".insertBeforeLast("_foo"))
        assertEquals("fi.le_foo.txt", "fi.le.txt".insertBeforeLast("_foo"))
        assertEquals("_foo", null.insertBeforeLast("_foo"))
    }

    @Test
    fun testEllipsizeThrowsAt0() {
        assertFailsWith<IllegalArgumentException> {
            "1234567890".ellipsize(0)
        }
    }

    @Test
    fun testEllipsizeTruncates() {
        assertEquals("1…", "1234567890".ellipsize(1))
        assertEquals("12345…", "1234567890".ellipsize(5))
    }

    @Test
    fun testEllipsizeNoop() {
        assertEquals("12345", "12345".ellipsize(5))
        assertEquals("123", "123".ellipsize(5))
    }

    @Test
    fun testReplacePrefix() {
        assertEquals("newHello", "oldHello".replacePrefix("old", "new"))
        assertEquals("world", "world".replacePrefix("hello", "new"))
    }

    @Test
    fun testWithBrackets() {
        assertEquals("(value)", "value".withBrackets())
        assertEquals("[value]", "value".withBrackets("[", "]"))
    }

    @Test
    fun testSafeCapitalize() {
        assertEquals("Hello", "hello".safeCapitalize())
        assertEquals("Hello", "Hello".safeCapitalize())
    }

    @Test
    fun testWithoutAccents() {
        assertEquals("Cafe", "Café".withoutAccents())
        assertEquals("naive", "naïve".withoutAccents())
    }

    // ---------- Unicode direction handling ----------

    @Test
    fun testContainsRtLOverride() {
        assertTrue("hello\u202Eworld".containsRtLOverride())
        assertFalse("hello world".containsRtLOverride())
    }

    @Test
    fun testEnsureEndsLeftToRight() {
        val text = "123\u202E456"
        assertEquals("$text\u202D", text.ensureEndsLeftToRight())
        val clean = "123456"
        assertEquals(clean, clean.ensureEndsLeftToRight())
    }

    @Test
    fun testFilterDirectionOverrides() {
        val text = "123\u202E456\u202D789"
        assertEquals("123456789", text.filterDirectionOverrides())
    }

    // ---------- Safe length ----------

    @Test
    fun testToSafeLengthNoEllipsize() {
        val input = "123456"
        assertEquals("123", input.toSafeLength(maxLength = 3, ellipsize = false))
    }

    @Test
    fun testToSafeLengthWithEllipsize() {
        val input = "123456"
        assertEquals("123…", input.toSafeLength(maxLength = 3, ellipsize = true))
    }

    @Test
    fun testToSafeLengthShorterThanMax() {
        val input = "123"
        assertEquals("123", input.toSafeLength(maxLength = 5))
    }
}
