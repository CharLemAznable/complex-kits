package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base62.base62;
import static com.github.charlemaznable.core.codec.Base62.base64;
import static com.github.charlemaznable.core.codec.Base62.unBase62;
import static com.github.charlemaznable.core.codec.Base62.unBase64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base62Test {

    @Test
    public void testBase64() {
        new Base62();

        assertEquals("MTIzNDU2Nzg5MA", base64(bytes("1234567890")));
    }

    @Test
    public void testUnBase64() {
        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA")));
    }

    @Test
    public void testBase62() {
        assertEquals("MTIzNDU2Nzg5MA", base62(bytes("1234567890")));
    }

    @Test
    public void testUnBase62() {
        assertEquals("1234567890", string(unBase62("MTIzNDU2Nzg5MA")));
    }
}
