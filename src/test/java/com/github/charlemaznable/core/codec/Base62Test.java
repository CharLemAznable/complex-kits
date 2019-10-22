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
        assertEquals("MTIzNDU2Nzg5MA", base64(bytes("1234567890")));
    }

    @Test
    public void testUnBase64() {
        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA")));
    }

    @Test
    public void testBase62() {
        assertEquals("MTIzNDU2Nzg5MA", base62(bytes("1234567890")));
        assertEquals("5Yqg5aibG6Kej5aibG5pWj5YiaX5ZOI5biaML1BIUOWFqOagiaOiaicmibmYtg",
                base62(bytes("加密解密散列哈希/PHP全栈进阶")));
    }

    @Test
    public void testUnBase62() {
        assertEquals("1234567890", string(unBase62("MTIzNDU2Nzg5MA")));
        assertEquals("加密解密散列哈希/PHP全栈进阶",
                string(unBase62("5Yqg5aibG6Kej5aibG5pWj5YiaX5ZOI5biaML1BIUOWFqOagiaOiaicmibmYtg")));
    }
}
