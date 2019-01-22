package com.github.charlemaznable.crypto;

import com.github.charlemaznable.lang.Rand;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Hex.hex;
import static com.github.charlemaznable.codec.Hex.unHex;
import static com.github.charlemaznable.crypto.AES.decrypt;
import static com.github.charlemaznable.crypto.AES.encrypt;
import static com.github.charlemaznable.lang.Rand.randLetters;
import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESTest {

    @Test
    public void testAES() {
        new AES();
        String key = String.valueOf(currentTimeMillis());
        assertEquals("123456", decrypt(encrypt("123456", key), key));

        key = randLetters(10);
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

    public void batchRun(int times) {
        String rand = Rand.randAlphanumeric(100);
        String key = String.valueOf(currentTimeMillis());

        for (int i = 0; i < times; ++i) {
            String src = rand + i;
            String enc = hex(encrypt(src, key));
            String dec = decrypt(unHex(enc), key);
            assertEquals(src, dec);
        }
    }

    public final int TIMES = 10000;

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
