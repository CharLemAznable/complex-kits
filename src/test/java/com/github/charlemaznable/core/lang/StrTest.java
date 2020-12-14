package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrTest {

    @Test
    public void testStr() {
        assertEquals("aaabbbccc", Str.padding("aaabbb", 'c', 3));
        assertEquals("aaabbb", Str.removeLastLetters("aaabbbccc", 'c'));

        assertTrue(Str.matches('(', ')'));
        assertTrue(Str.matches('[', ']'));
        assertTrue(Str.matches('{', '}'));

        assertEquals("bbb", Str.substrInQuotes("aaa(bbb)ccc", '(', 0));
        assertEquals("(bbb)", Str.substrInQuotes("aaa((bbb))ccc", '(', 0));
        assertEquals("", Str.substrInQuotes("aaa((bbb)ccc", '(', 0));

        assertTrue(Str.isInteger("12"));
        assertTrue(Str.isInteger("2147483647"));
        assertFalse(Str.isInteger("2147483648"));

        assertTrue(Str.isLong("12"));
        assertTrue(Str.isLong("2147483647"));
        assertTrue(Str.isLong("2147483648"));
        assertTrue(Str.isLong("9223372036854775807"));
        assertFalse(Str.isLong("9223372036854775808"));
    }

    @Test
    public void testParse() {
        assertEquals(0, Str.intOf(""));
        assertEquals(0, Str.intOf("AA"));
        assertEquals(0, Str.intOf("!@#"));
        assertEquals(123, Str.intOf("123"));
        assertEquals(-123, Str.intOf("-123"));

        assertEquals(0L, Str.longOf(""));
        assertEquals(0L, Str.longOf("AA"));
        assertEquals(0L, Str.longOf("!@#"));
        assertEquals(123L, Str.longOf("123"));
        assertEquals(-123L, Str.longOf("-123"));

        assertEquals((short) 0, Str.shortOf(""));
        assertEquals((short) 0, Str.shortOf("AA"));
        assertEquals((short) 0, Str.shortOf("!@#"));
        assertEquals((short) 123, Str.shortOf("123"));
        assertEquals((short) -123, Str.shortOf("-123"));

        assertEquals(0F, Str.floatOf(""));
        assertEquals(0F, Str.floatOf("AA"));
        assertEquals(0F, Str.floatOf("!@#"));
        assertEquals(0F, Str.floatOf("123.4.5"));
        assertEquals(123F, Str.floatOf("123"));
        assertEquals(-123F, Str.floatOf("-123"));
        assertEquals(123.45F, Str.floatOf("123.45"));
        assertEquals(-123.45F, Str.floatOf("-123.45"));

        assertEquals(0D, Str.doubleOf(""));
        assertEquals(0D, Str.doubleOf("AA"));
        assertEquals(0D, Str.doubleOf("!@#"));
        assertEquals(0D, Str.doubleOf("123.4.5"));
        assertEquals(123D, Str.doubleOf("123"));
        assertEquals(-123D, Str.doubleOf("-123"));
        assertEquals(123.45D, Str.doubleOf("123.45"));
        assertEquals(-123.45D, Str.doubleOf("-123.45"));
    }

    @Test
    public void testCompareDotSplitSerialNumber() {
        assertEquals(0, Str.compareDotSplitSerialNumber(null, null));
        assertEquals(-1, Str.compareDotSplitSerialNumber(null, "abc"));
        assertEquals(1, Str.compareDotSplitSerialNumber(null, "abc", false));
        assertEquals(1, Str.compareDotSplitSerialNumber("abc", null));
        assertEquals(-1, Str.compareDotSplitSerialNumber("abc", null, false));
        assertEquals(0, Str.compareDotSplitSerialNumber("abc", "abc"));

        assertEquals(-1, Str.compareDotSplitSerialNumber("1.0", "1.1"));
        assertEquals(-1, Str.compareDotSplitSerialNumber("1.9", "1.10"));
        assertEquals(-1, Str.compareDotSplitSerialNumber("1.9", "1.9.1"));
    }
}
