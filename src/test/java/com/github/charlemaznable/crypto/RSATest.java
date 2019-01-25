package com.github.charlemaznable.crypto;


import com.github.charlemaznable.lang.Rand;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Hex.hex;
import static com.github.charlemaznable.codec.Hex.unHex;
import static com.github.charlemaznable.crypto.RSA.*;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RSATest {

    @Test
    public void testRSA() {
        val plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        val keyPair = generateKeyPair();
        val publicKeyString = getPublicKeyString(keyPair);
        val privateKeyString = getPrivateKeyString(keyPair);

        val publicKey = publicKey(publicKeyString);
        val privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    @Test
    public void testRSA2() {
        val plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        val keyPair = generateKeyPair(2048);
        val publicKeyString = getPublicKeyString(keyPair);
        val privateKeyString = getPrivateKeyString(keyPair);

        val publicKey = publicKey(publicKeyString);
        val privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    public void batchRun(int times) {
        val rand = Rand.randAlphanumeric(100);
        val keyPair = generateKeyPair();
        val publicKey = getPublicKey(keyPair);
        val privateKey = getPrivateKey(keyPair);

        for (var i = 0; i < times; ++i) {
            val plainText = rand + i;

            val enc1 = hex(pubEncrypt(plainText, publicKey));
            val dec1 = prvDecrypt(unHex(enc1), privateKey);
            assertEquals(plainText, dec1);

            val enc2 = hex(prvEncrypt(plainText, privateKey));
            val dec2 = pubDecrypt(unHex(enc2), publicKey);
            assertEquals(plainText, dec2);
        }
    }

    public final int TIMES = 10000;

    @SneakyThrows
    public void routineRun(int threads) {
        val service = new Thread[threads];
        for (var i = 0; i < threads; i++) {
            service[i] = new Thread(() -> batchRun(TIMES));
            service[i].start();
        }

        for (var i = 0; i < threads; i++) {
            service[i].join();
        }
    }

    @Test
    public void testRSABatch() {
        routineRun(getRuntime().availableProcessors() + 1);
    }
}
