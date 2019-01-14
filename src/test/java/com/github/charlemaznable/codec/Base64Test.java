package com.github.charlemaznable.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Base64.unBase64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64Test {

    @Test
    public void testBase64() {
        assertEquals("MTIzNDU2Nzg5MA==", base64(bytes("1234567890")));
    }

    @Test
    public void testUnBase64() {
        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA==")));
    }
}
