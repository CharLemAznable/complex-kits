package com.github.charlemaznable.core.codec;

import lombok.val;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;

import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.DigestHMAC.MD5;
import static com.github.charlemaznable.core.codec.DigestHMAC.SHA1;
import static com.github.charlemaznable.core.codec.DigestHMAC.SHA256;
import static com.github.charlemaznable.core.codec.DigestHMAC.SHA512;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.github.charlemaznable.core.lang.Rand.randAlphanumeric;
import static javax.crypto.Mac.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestHMACTest {

    @Test
    public void testDigestMACHex() throws Exception {
        val key = randAlphanumeric(32);

        val hashMD5Hex = MD5.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher", key));
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher", bytes(key)));
        assertEquals(hashMD5Hex, hex(MD5.digest("可以提供有状态的Hasher", key)));
        assertEquals(hashMD5Hex, hex(MD5.digest("可以提供有状态的Hasher", bytes(key))));
        val hmacMD5 = getInstance("HmacMD5");
        hmacMD5.init(new SecretKeySpec(bytes(key), "HmacMD5"));
        assertEquals(hashMD5Hex, hex(hmacMD5.doFinal(bytes("可以提供有状态的Hasher"))));

        val hashSHA1Hex = SHA1.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher", key));
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher", bytes(key)));
        assertEquals(hashSHA1Hex, hex(SHA1.digest("可以提供有状态的Hasher", key)));
        assertEquals(hashSHA1Hex, hex(SHA1.digest("可以提供有状态的Hasher", bytes(key))));
        val hmacSHA1 = getInstance("HmacSHA1");
        hmacSHA1.init(new SecretKeySpec(bytes(key), "HmacSHA1"));
        assertEquals(hashSHA1Hex, hex(hmacSHA1.doFinal(bytes("可以提供有状态的Hasher"))));

        val hashSHA256Hex = SHA256.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher", key));
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher", bytes(key)));
        assertEquals(hashSHA256Hex, hex(SHA256.digest("可以提供有状态的Hasher", key)));
        assertEquals(hashSHA256Hex, hex(SHA256.digest("可以提供有状态的Hasher", bytes(key))));

        val hashSHA512Hex = SHA512.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher", key));
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher", bytes(key)));
        assertEquals(hashSHA512Hex, hex(SHA512.digest("可以提供有状态的Hasher", key)));
        assertEquals(hashSHA512Hex, hex(SHA512.digest("可以提供有状态的Hasher", bytes(key))));
    }

    @Test
    public void testDigestMACBase64() throws Exception {
        val key = randAlphanumeric(32);

        val hashMD5Base64 = MD5.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher", key));
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher", bytes(key)));
        val hmacMD5 = getInstance("HmacMD5");
        hmacMD5.init(new SecretKeySpec(bytes(key), "HmacMD5"));
        assertEquals(hashMD5Base64, base64(hmacMD5.doFinal(bytes("可以提供有状态的Hasher"))));

        val hashSHA1Base64 = SHA1.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher", key));
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher", bytes(key)));
        val hmacSHA1 = getInstance("HmacSHA1");
        hmacSHA1.init(new SecretKeySpec(bytes(key), "HmacSHA1"));
        assertEquals(hashSHA1Base64, base64(hmacSHA1.doFinal(bytes("可以提供有状态的Hasher"))));

        val hashSHA256Base64 = SHA256.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher", key));
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher", bytes(key)));

        val hashSHA512Base64 = SHA512.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA512Base64, SHA512.digestBase64("可以提供有状态的Hasher", key));
        assertEquals(hashSHA512Base64, SHA512.digestBase64("可以提供有状态的Hasher", bytes(key)));
    }
}
