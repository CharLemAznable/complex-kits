package com.github.charlemaznable.core.crypto;

import com.github.charlemaznable.core.lang.Rand;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;

import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.github.charlemaznable.core.codec.Hex.unHex;
import static com.github.charlemaznable.core.crypto.AES.decrypt;
import static com.github.charlemaznable.core.crypto.AES.encrypt;
import static com.github.charlemaznable.core.lang.Rand.randLetters;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AESTest {

    public final int TIMES = 10000;

    @Test
    public void testAES() {
        String key = String.valueOf(currentTimeMillis());
        assertEquals("123456", decrypt(encrypt("123456", key), key));

        key = randLetters(10);
        assertEquals(10, key.length());
        assertEquals(hex(encrypt("12345", key)), hex(encrypt("12345", key)));
        assertEquals(hex(encrypt("123456", key)), hex(encrypt("123456", key)));
        assertEquals(hex(encrypt("1234567", key)), hex(encrypt("1234567", key)));
        assertEquals(hex(encrypt("汉", key)), hex(encrypt("汉", key)));
        assertEquals(hex(encrypt("中文", key)), hex(encrypt("中文", key)));

        val key2 = key + key;
        assertEquals(hex(encrypt("The quick brown fox jumps over the lazy dog", key)),
                hex(encrypt("The quick brown fox jumps over the lazy dog", key2)));
    }

    @Test
    public void testAESWithKeySize() {
        String key = String.valueOf(currentTimeMillis());
        val keySize = 256;
        assertEquals("123456", decrypt(encrypt("123456", key, keySize), key, keySize));

        key = randLetters(10);
        assertEquals(10, key.length());
        assertEquals(hex(encrypt("12345", key, keySize)), hex(encrypt("12345", key, keySize)));
        assertEquals(hex(encrypt("123456", key, keySize)), hex(encrypt("123456", key, keySize)));
        assertEquals(hex(encrypt("1234567", key, keySize)), hex(encrypt("1234567", key, keySize)));
        assertEquals(hex(encrypt("汉", key, keySize)), hex(encrypt("汉", key, keySize)));
        assertEquals(hex(encrypt("中文", key, keySize)), hex(encrypt("中文", key, keySize)));

        val key2 = key + key;
        assertEquals(hex(encrypt("The quick brown fox jumps over the lazy dog", key, keySize)),
                hex(encrypt("The quick brown fox jumps over the lazy dog", key2, keySize)));
    }

    @Test
    public void testAESError() {
        val key = String.valueOf(currentTimeMillis());
        assertThrows(InvalidKeyException.class, () -> encrypt("123456", key, 64));
        assertThrows(InvalidKeyException.class, () -> decrypt(encrypt("123456", key), key, 64));
    }

    public void batchRun(int times) {
        val rand = Rand.randAlphanumeric(100);
        val key = String.valueOf(currentTimeMillis());

        for (int i = 0; i < times; ++i) {
            val src = rand + i;
            val enc = hex(encrypt(src, key));
            val dec = decrypt(unHex(enc), key);
            assertEquals(src, dec);
        }
    }

    @SneakyThrows
    public void routineRun(int threads) {
        val service = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            service[i] = new Thread(() -> batchRun(TIMES));
            service[i].start();
        }

        for (int i = 0; i < threads; i++) {
            service[i].join();
        }
    }

    @Test
    public void testAESBatch() {
        routineRun(getRuntime().availableProcessors() + 1);
    }
}
