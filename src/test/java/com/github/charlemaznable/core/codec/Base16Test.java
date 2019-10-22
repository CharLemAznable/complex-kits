package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base16.base16;
import static com.github.charlemaznable.core.codec.Base16.base16FromString;
import static com.github.charlemaznable.core.codec.Base16.unBase16;
import static com.github.charlemaznable.core.codec.Base16.unBase16AsString;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base16Test {

    @Test
    public void testBase16() {
        assertEquals("01020304", base16(new byte[]{0x01, 0x02, 0x03, 0x04}));
        assertEquals("6162636465666768", base16FromString("abcdefgh"));
    }

    @Test
    public void testUnBase16() {
        assertEquals("abcdefgh", unBase16AsString("6162636465666768"));
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04}, unBase16("01020304"));
    }
}
