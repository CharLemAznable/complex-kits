package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrTest {

    @Test
    public void testStr() {
        new Str();

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
}
