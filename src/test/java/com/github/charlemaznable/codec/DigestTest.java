package com.github.charlemaznable.codec;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Digest.MD5;
import static com.github.charlemaznable.codec.Digest.SHA1;
import static com.github.charlemaznable.codec.Digest.SHA256;
import static com.github.charlemaznable.codec.Digest.SHA384;
import static com.github.charlemaznable.codec.Digest.SHA512;
import static com.github.charlemaznable.codec.Hex.hex;
import static java.security.MessageDigest.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestTest {

    @Test
    @Deprecated
    public void testDigestHex() throws NoSuchAlgorithmException {
        String hashMD5Hex = MD5.digestHex("可以提供有状态的Hasher");
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashMD5Hex, hex(getInstance("MD5").digest(bytes("可以提供有状态的Hasher"))));

        String hashSHA1Hex = SHA1.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA1Hex, hex(getInstance("SHA1").digest(bytes("可以提供有状态的Hasher"))));

        String hashSHA256Hex = SHA256.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher"));

        String hashSHA384Hex = SHA384.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA384Hex, SHA384.digestHex("可以提供有状态的Hasher"));

        String hashSHA512Hex = SHA512.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher"));
    }

    @Test
    @Deprecated
    public void testDigestSaltHex() throws NoSuchAlgorithmException {
        String hashMD5Hex = MD5.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashMD5Hex, hex(getInstance("MD5").digest(bytes("salt可以提供有状态的Hashersalt"))));

        String hashSHA1Hex = SHA1.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA1Hex, hex(getInstance("SHA1").digest(bytes("salt可以提供有状态的Hashersalt"))));

        String hashSHA256Hex = SHA256.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher", "salt"));

        String hashSHA384Hex = SHA384.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Hex, SHA384.digestHex("可以提供有状态的Hasher", "salt"));

        String hashSHA512Hex = SHA512.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher", "salt"));
    }

    @Test
    @Deprecated
    public void testDigestBase64() throws NoSuchAlgorithmException {
        String hashMD5Base64 = MD5.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher"));
        assertEquals(hashMD5Base64, base64(getInstance("MD5").digest(bytes("可以提供有状态的Hasher"))));

        String hashSHA1Base64 = SHA1.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher"));
        assertEquals(hashSHA1Base64, base64(getInstance("SHA1").digest(bytes("可以提供有状态的Hasher"))));

        String hashSHA256Base64 = SHA256.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher"));

        String hashSHA384Base64 = SHA256.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA384Base64, SHA256.digestBase64("可以提供有状态的Hasher"));

        String hashSHA512Base64 = SHA256.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA512Base64, SHA256.digestBase64("可以提供有状态的Hasher"));
    }

    @Test
    @Deprecated
    public void testDigestSaltBase64() throws NoSuchAlgorithmException {
        String hashMD5Base64 = MD5.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashMD5Base64, base64(getInstance("MD5").digest(bytes("salt可以提供有状态的Hashersalt"))));

        String hashSHA1Base64 = SHA1.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA1Base64, base64(getInstance("SHA1").digest(bytes("salt可以提供有状态的Hashersalt"))));

        String hashSHA256Base64 = SHA256.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher", "salt"));

        String hashSHA384Base64 = SHA256.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Base64, SHA256.digestBase64("可以提供有状态的Hasher", "salt"));

        String hashSHA512Base64 = SHA256.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Base64, SHA256.digestBase64("可以提供有状态的Hasher", "salt"));
    }
}
