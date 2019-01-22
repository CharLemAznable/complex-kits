package com.github.charlemaznable.crypto;


import com.github.charlemaznable.lang.Rand;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.github.charlemaznable.codec.Hex.hex;
import static com.github.charlemaznable.codec.Hex.unHex;
import static com.github.charlemaznable.crypto.RSA.*;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RSATest {

    @Test
    public void testRSA() {
        String plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        KeyPair keyPair = generateKeyPair();
        String publicKeyString = getPublicKeyString(keyPair);
        String privateKeyString = getPrivateKeyString(keyPair);

        PublicKey publicKey = publicKey(publicKeyString);
        PrivateKey privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    @Test
    public void testRSA2() {
        String plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        KeyPair keyPair = generateKeyPair(2048);
        String publicKeyString = getPublicKeyString(keyPair);
        String privateKeyString = getPrivateKeyString(keyPair);

        PublicKey publicKey = publicKey(publicKeyString);
        PrivateKey privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    public void batchRun(int times) {
        String rand = Rand.randAlphanumeric(100);
        KeyPair keyPair = generateKeyPair();
        PublicKey publicKey = getPublicKey(keyPair);
        PrivateKey privateKey = getPrivateKey(keyPair);

        for (int i = 0; i < times; ++i) {
            String plainText = rand + i;

            String enc1 = hex(pubEncrypt(plainText, publicKey));
            String dec1 = prvDecrypt(unHex(enc1), privateKey);
            assertEquals(plainText, dec1);

            String enc2 = hex(prvEncrypt(plainText, privateKey));
            String dec2 = pubDecrypt(unHex(enc2), publicKey);
            assertEquals(plainText, dec2);
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
    public void testRSABatch() {
        routineRun(getRuntime().availableProcessors() + 1);
    }
}
