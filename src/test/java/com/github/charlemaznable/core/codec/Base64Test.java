package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base64.Format.PURIFIED;
import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Base64.unBase64;
import static com.github.charlemaznable.core.codec.Base64.unBase64AsString;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base64Test {

    @Test
    public void testBase64() {
        assertEquals("MTIzNDU2Nzg5MA==", base64(bytes("1234567890")));
        assertEquals("MTIzNDU2Nzg5MA==", base64("1234567890"));

        assertEquals("MTIzNDU2Nzg5MA", base64(bytes("1234567890"), PURIFIED));
        assertEquals("MTIzNDU2Nzg5MA", base64("1234567890", PURIFIED));
    }

    @Test
    public void testUnBase64() {
        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA==")));
        assertEquals("1234567890", unBase64AsString("MTIzNDU2Nzg5MA=="));

        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA")));
        assertEquals("1234567890", unBase64AsString("MTIzNDU2Nzg5MA"));
    }
}
