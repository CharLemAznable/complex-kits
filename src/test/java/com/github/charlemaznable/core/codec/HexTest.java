package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.github.charlemaznable.core.codec.Hex.unHex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexTest {

    @Test
    public void testHex() {
        assertEquals("54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67",
                hex(bytes("The quick brown fox jumps over the lazy dog")));
        assertEquals(hex(bytes("The quick brown fox jumps over the lazy dog")),
                hex(bytes("The quick brown fox jumps over the lazy dog")));
        assertEquals("The quick brown fox jumps over the lazy dog",
                string(unHex(hex(bytes("The quick brown fox jumps over the lazy dog")))));
    }
}
