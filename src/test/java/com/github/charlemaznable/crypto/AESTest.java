package com.github.charlemaznable.crypto;

import com.github.charlemaznable.lang.Rand;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Hex.hex;
import static com.github.charlemaznable.crypto.AES.decrypt;
import static com.github.charlemaznable.crypto.AES.encrypt;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESTest {

    @Test
    public void testAES() {
        new AES();
        String key = String.valueOf(System.currentTimeMillis());
        assertEquals("123456", decrypt(encrypt("123456", key), key));

        key = Rand.randLetters(10);
        assertEquals(10, key.length());
        assertEquals(hex(encrypt("12345", key)), hex(encrypt("12345", key)));
        assertEquals(hex(encrypt("123456", key)), hex(encrypt("123456", key)));
        assertEquals(hex(encrypt("1234567", key)), hex(encrypt("1234567", key)));
        assertEquals(hex(encrypt("汉", key)), hex(encrypt("汉", key)));
        assertEquals(hex(encrypt("中文", key)), hex(encrypt("中文", key)));

        String key2 = key + key;
        assertEquals(hex(encrypt("The quick brown fox jumps over the lazy dog", key2)),
                hex(encrypt("The quick brown fox jumps over the lazy dog", key2)));
    }
}
