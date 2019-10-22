package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base64.Format.PURIFIED;
import static com.github.charlemaznable.core.codec.Base64.Format.URL_SAFE;
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

        assertEquals("MTIzNDU2Nzg5MA", base64(bytes("1234567890"), URL_SAFE));
        assertEquals("MTIzNDU2Nzg5MA", base64("1234567890", URL_SAFE));

        assertEquals("MTIzNDU2Nzg5MA", base64(bytes("1234567890"), PURIFIED));
        assertEquals("MTIzNDU2Nzg5MA", base64("1234567890", PURIFIED));

        assertEquals("5Yqg5a+G6Kej5a+G5pWj5YiX5ZOI5biML1BIUOWFqOagiOi/m+mYtg==",
                base64(bytes("加密解密散列哈希/PHP全栈进阶")));
    }

    @Test
    public void testUnBase64() {
        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA==")));
        assertEquals("1234567890", unBase64AsString("MTIzNDU2Nzg5MA=="));

        assertEquals("1234567890", string(unBase64("MTIzNDU2Nzg5MA")));
        assertEquals("1234567890", unBase64AsString("MTIzNDU2Nzg5MA"));

        assertEquals("加密解密散列哈希/PHP全栈进阶",
                string(unBase64("5Yqg5a+G6Kej5a+G5pWj5YiX5ZOI5biML1BIUOWFqOagiOi/m+mYtg==")));
    }
}
