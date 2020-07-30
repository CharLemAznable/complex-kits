package com.github.charlemaznable.core.crypto;

import com.github.charlemaznable.core.lang.Rand;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.security.spec.InvalidKeySpecException;

import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.github.charlemaznable.core.codec.Hex.unHex;
import static com.github.charlemaznable.core.crypto.RSA.generateKeyPair;
import static com.github.charlemaznable.core.crypto.RSA.getPrivateKey;
import static com.github.charlemaznable.core.crypto.RSA.getPrivateKeyString;
import static com.github.charlemaznable.core.crypto.RSA.getPublicKey;
import static com.github.charlemaznable.core.crypto.RSA.getPublicKeyString;
import static com.github.charlemaznable.core.crypto.RSA.privateKey;
import static com.github.charlemaznable.core.crypto.RSA.privateKeySize;
import static com.github.charlemaznable.core.crypto.RSA.prvDecrypt;
import static com.github.charlemaznable.core.crypto.RSA.prvEncrypt;
import static com.github.charlemaznable.core.crypto.RSA.pubDecrypt;
import static com.github.charlemaznable.core.crypto.RSA.pubEncrypt;
import static com.github.charlemaznable.core.crypto.RSA.publicKey;
import static com.github.charlemaznable.core.crypto.RSA.publicKeySize;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RSATest {

    public final int TIMES = 10000;

    @Test
    public void testRSA() {
        var plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        var keyPair = generateKeyPair();
        var publicKeyString = getPublicKeyString(keyPair);
        var privateKeyString = getPrivateKeyString(keyPair);

        var publicKey = publicKey(publicKeyString);
        var privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));

        assertThrows(InvalidKeySpecException.class, () -> publicKey(privateKeyString));
        assertThrows(InvalidKeySpecException.class, () -> privateKey(publicKeyString));

        assertThrows(InvalidKeySpecException.class, () -> publicKeySize(null));
        assertThrows(InvalidKeySpecException.class, () -> privateKeySize(null));
    }

    @Test
    public void testRSA2() {
        var plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        var keyPair = generateKeyPair(2048);
        var publicKeyString = getPublicKeyString(keyPair);
        var privateKeyString = getPrivateKeyString(keyPair);

        var publicKey = publicKey(publicKeyString);
        var privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    public void batchRun(int times) {
        var rand = Rand.randAlphanumeric(100);
        var keyPair = generateKeyPair();
        var publicKey = getPublicKey(keyPair);
        var privateKey = getPrivateKey(keyPair);

        for (var i = 0; i < times; ++i) {
            var plainText = rand + i;

            var enc1 = hex(pubEncrypt(plainText, publicKey));
            var dec1 = prvDecrypt(unHex(enc1), privateKey);
            assertEquals(plainText, dec1);

            var enc2 = hex(prvEncrypt(plainText, privateKey));
            var dec2 = pubDecrypt(unHex(enc2), publicKey);
            assertEquals(plainText, dec2);
        }
    }

    @SneakyThrows
    public void routineRun(int threads) {
        var service = new Thread[threads];
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
