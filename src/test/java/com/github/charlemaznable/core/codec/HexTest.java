package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HexTest {

    @Test
    public void testHex() {
        assertEquals(hex(bytes("The quick brown fox jumps over the lazy dog")),
                hex(bytes("The quick brown fox jumps over the lazy dog")));

    }
}
