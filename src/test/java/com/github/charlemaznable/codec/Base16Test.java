package com.github.charlemaznable.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base16Test {

    @Test
    public void testBase16() {
        assertEquals("6162636465666768", Base16.base16FromString("abcdefgh"));
    }

    @Test
    public void testUnBase16() {
        assertEquals("abcdefgh", Base16.unBase16AsString("6162636465666768"));
    }
}
