package com.github.charlemaznable.core.codec;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Digest.MD5;
import static com.github.charlemaznable.core.codec.Digest.SHA1;
import static com.github.charlemaznable.core.codec.Digest.SHA256;
import static com.github.charlemaznable.core.codec.Digest.SHA384;
import static com.github.charlemaznable.core.codec.Digest.SHA512;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static java.security.MessageDigest.getInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestTest {

    @Test
    @Deprecated
    public void testDigestHex() throws NoSuchAlgorithmException {
        val hashMD5Hex = MD5.digestHex("可以提供有状态的Hasher");
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashMD5Hex, hex(MD5.digest("可以提供有状态的Hasher")));
        assertEquals(hashMD5Hex, hex(getInstance("MD5").digest(bytes("可以提供有状态的Hasher"))));

        val hashSHA1Hex = SHA1.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA1Hex, hex(SHA1.digest("可以提供有状态的Hasher")));
        assertEquals(hashSHA1Hex, hex(getInstance("SHA1").digest(bytes("可以提供有状态的Hasher"))));

        val hashSHA256Hex = SHA256.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA256Hex, hex(SHA256.digest("可以提供有状态的Hasher")));

        val hashSHA384Hex = SHA384.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA384Hex, SHA384.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA384Hex, hex(SHA384.digest("可以提供有状态的Hasher")));

        val hashSHA512Hex = SHA512.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA512Hex, hex(SHA512.digest("可以提供有状态的Hasher")));
    }

    @Test
    @Deprecated
    public void testDigestSaltHex() throws NoSuchAlgorithmException {
        val hashMD5Hex = MD5.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashMD5Hex, MD5.digestHex(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashMD5Hex, MD5.digestHex("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashMD5Hex, hex(MD5.digest("可以提供有状态的Hasher", "salt")));
        assertEquals(hashMD5Hex, hex(MD5.digest(bytes("可以提供有状态的Hasher"), "salt")));
        assertEquals(hashMD5Hex, hex(MD5.digest("可以提供有状态的Hasher", bytes("salt"))));
        assertEquals(hashMD5Hex, hex(getInstance("MD5").digest(bytes("salt可以提供有状态的Hashersalt"))));

        val hashSHA1Hex = SHA1.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA1Hex, SHA1.digestHex(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA1Hex, SHA1.digestHex("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashSHA1Hex, hex(SHA1.digest("可以提供有状态的Hasher", "salt")));
        assertEquals(hashSHA1Hex, hex(SHA1.digest(bytes("可以提供有状态的Hasher"), "salt")));
        assertEquals(hashSHA1Hex, hex(SHA1.digest("可以提供有状态的Hasher", bytes("salt"))));
        assertEquals(hashSHA1Hex, hex(getInstance("SHA1").digest(bytes("salt可以提供有状态的Hashersalt"))));

        val hashSHA256Hex = SHA256.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA256Hex, SHA256.digestHex(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA256Hex, SHA256.digestHex("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashSHA256Hex, hex(SHA256.digest("可以提供有状态的Hasher", "salt")));
        assertEquals(hashSHA256Hex, hex(SHA256.digest(bytes("可以提供有状态的Hasher"), "salt")));
        assertEquals(hashSHA256Hex, hex(SHA256.digest("可以提供有状态的Hasher", bytes("salt"))));

        val hashSHA384Hex = SHA384.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Hex, SHA384.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA384Hex, SHA384.digestHex(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA384Hex, SHA384.digestHex("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashSHA384Hex, hex(SHA384.digest("可以提供有状态的Hasher", "salt")));
        assertEquals(hashSHA384Hex, hex(SHA384.digest(bytes("可以提供有状态的Hasher"), "salt")));
        assertEquals(hashSHA384Hex, hex(SHA384.digest("可以提供有状态的Hasher", bytes("salt"))));

        val hashSHA512Hex = SHA512.digestHex("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA512Hex, SHA512.digestHex(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA512Hex, SHA512.digestHex("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashSHA512Hex, hex(SHA512.digest("可以提供有状态的Hasher", "salt")));
        assertEquals(hashSHA512Hex, hex(SHA512.digest(bytes("可以提供有状态的Hasher"), "salt")));
        assertEquals(hashSHA512Hex, hex(SHA512.digest("可以提供有状态的Hasher", bytes("salt"))));
    }

    @Test
    @Deprecated
    public void testDigestBase64() throws NoSuchAlgorithmException {
        val hashMD5Base64 = MD5.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher"));
        assertEquals(hashMD5Base64, base64(getInstance("MD5").digest(bytes("可以提供有状态的Hasher"))));

        val hashSHA1Base64 = SHA1.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher"));
        assertEquals(hashSHA1Base64, base64(getInstance("SHA1").digest(bytes("可以提供有状态的Hasher"))));

        val hashSHA256Base64 = SHA256.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher"));

        val hashSHA384Base64 = SHA384.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA384Base64, SHA384.digestBase64("可以提供有状态的Hasher"));

        val hashSHA512Base64 = SHA512.digestBase64("可以提供有状态的Hasher");
        assertEquals(hashSHA512Base64, SHA512.digestBase64("可以提供有状态的Hasher"));
    }

    @Test
    @Deprecated
    public void testDigestSaltBase64() throws NoSuchAlgorithmException {
        val hashMD5Base64 = MD5.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashMD5Base64, MD5.digestBase64(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashMD5Base64, MD5.digestBase64("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashMD5Base64, base64(getInstance("MD5").digest(bytes("salt可以提供有状态的Hashersalt"))));

        val hashSHA1Base64 = SHA1.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA1Base64, SHA1.digestBase64(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA1Base64, SHA1.digestBase64("可以提供有状态的Hasher", bytes("salt")));
        assertEquals(hashSHA1Base64, base64(getInstance("SHA1").digest(bytes("salt可以提供有状态的Hashersalt"))));

        val hashSHA256Base64 = SHA256.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA256Base64, SHA256.digestBase64(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA256Base64, SHA256.digestBase64("可以提供有状态的Hasher", bytes("salt")));

        val hashSHA384Base64 = SHA384.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Base64, SHA384.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA384Base64, SHA384.digestBase64(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA384Base64, SHA384.digestBase64("可以提供有状态的Hasher", bytes("salt")));

        val hashSHA512Base64 = SHA512.digestBase64("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Base64, SHA512.digestBase64("可以提供有状态的Hasher", "salt"));
        assertEquals(hashSHA512Base64, SHA512.digestBase64(bytes("可以提供有状态的Hasher"), "salt"));
        assertEquals(hashSHA512Base64, SHA512.digestBase64("可以提供有状态的Hasher", bytes("salt")));
    }

    @Test
    @Deprecated
    public void testDigestDeprecated() {
        val hashMD5Hex = MD5.digestHexDeprecated("可以提供有状态的Hasher");
        assertEquals(hashMD5Hex, MD5.digestHexDeprecated("可以提供有状态的Hasher"));

        val hashSHA1Hex = SHA1.digestHexDeprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA1Hex, SHA1.digestHexDeprecated("可以提供有状态的Hasher"));

        val hashSHA256Hex = SHA256.digestHexDeprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA256Hex, SHA256.digestHexDeprecated("可以提供有状态的Hasher"));

        val hashSHA384Hex = SHA384.digestHexDeprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA384Hex, SHA384.digestHexDeprecated("可以提供有状态的Hasher"));

        val hashSHA512Hex = SHA512.digestHexDeprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA512Hex, SHA512.digestHexDeprecated("可以提供有状态的Hasher"));

        val hashMD5Base64 = MD5.digestBase64Deprecated("可以提供有状态的Hasher");
        assertEquals(hashMD5Base64, MD5.digestBase64Deprecated("可以提供有状态的Hasher"));

        val hashSHA1Base64 = SHA1.digestBase64Deprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA1Base64, SHA1.digestBase64Deprecated("可以提供有状态的Hasher"));

        val hashSHA256Base64 = SHA256.digestBase64Deprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA256Base64, SHA256.digestBase64Deprecated("可以提供有状态的Hasher"));

        val hashSHA384Base64 = SHA384.digestBase64Deprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA384Base64, SHA384.digestBase64Deprecated("可以提供有状态的Hasher"));

        val hashSHA512Base64 = SHA512.digestBase64Deprecated("可以提供有状态的Hasher");
        assertEquals(hashSHA512Base64, SHA512.digestBase64Deprecated("可以提供有状态的Hasher"));
    }

    @Test
    @Deprecated
    public void testDigestSaltDeprecated() {
        val hashMD5Hex = MD5.digestHexDeprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Hex, MD5.digestHexDeprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA1Hex = SHA1.digestHexDeprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Hex, SHA1.digestHexDeprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA256Hex = SHA256.digestHexDeprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Hex, SHA256.digestHexDeprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA384Hex = SHA384.digestHexDeprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Hex, SHA384.digestHexDeprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA512Hex = SHA512.digestHexDeprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Hex, SHA512.digestHexDeprecated("可以提供有状态的Hasher", "salt"));

        val hashMD5Base64 = MD5.digestBase64Deprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashMD5Base64, MD5.digestBase64Deprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA1Base64 = SHA1.digestBase64Deprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA1Base64, SHA1.digestBase64Deprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA256Base64 = SHA256.digestBase64Deprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA256Base64, SHA256.digestBase64Deprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA384Base64 = SHA384.digestBase64Deprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA384Base64, SHA384.digestBase64Deprecated("可以提供有状态的Hasher", "salt"));

        val hashSHA512Base64 = SHA512.digestBase64Deprecated("可以提供有状态的Hasher", "salt");
        assertEquals(hashSHA512Base64, SHA512.digestBase64Deprecated("可以提供有状态的Hasher", "salt"));
    }
}
