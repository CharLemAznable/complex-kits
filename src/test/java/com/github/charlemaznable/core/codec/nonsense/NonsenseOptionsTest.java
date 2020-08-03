package com.github.charlemaznable.core.codec.nonsense;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonsenseOptionsTest {

    @Test
    public void testNonsenseOptions() {
        val options = new NonsenseOptions();

        assertEquals("nonsense", options.key());
        assertEquals(16, options.count());
        assertEquals(0, options.start());
        assertEquals(0, options.end());
        assertTrue(options.letters());
        assertTrue(options.numbers());
        assertNull(options.chars());

        val charStr = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        options.key("nonce").count(32).start(32).end(127)
                .letters(false).numbers(false)
                .chars(charStr.toCharArray());

        assertEquals("nonce", options.key());
        assertEquals(32, options.count());
        assertEquals(32, options.start());
        assertEquals(127, options.end());
        assertFalse(options.letters());
        assertFalse(options.numbers());
        assertEquals(charStr, new String(options.chars()));
    }
}
