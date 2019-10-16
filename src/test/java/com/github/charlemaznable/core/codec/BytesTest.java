package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BytesTest {

    @Test
    public void testBytes() {
        assertNull(bytes(null));
    }
}
