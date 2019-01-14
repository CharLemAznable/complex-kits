package com.github.charlemaznable.codec;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestHMACTest {

    @Test
    public void testDigestMACHex() throws Exception {
        String key = "192006250b4c09247ec02edce69f6a2d";

        String hashMD5Hex = DigestHMAC.MD5.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashMD5Hex, DigestHMAC.MD5.digestHex("可以提供有状态的Hasher", key));
        Mac HmacMD5 = Mac.getInstance("HmacMD5");
        HmacMD5.init(new SecretKeySpec(bytes(key), "HmacMD5"));
        assertEquals(hashMD5Hex, hex(HmacMD5.doFinal(bytes("可以提供有状态的Hasher"))));

        String hashSHA1Hex = DigestHMAC.SHA1.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA1Hex, DigestHMAC.SHA1.digestHex("可以提供有状态的Hasher", key));
        Mac HmacSHA1 = Mac.getInstance("HmacSHA1");
        HmacSHA1.init(new SecretKeySpec(bytes(key), "HmacSHA1"));
        assertEquals(hashSHA1Hex, hex(HmacSHA1.doFinal(bytes("可以提供有状态的Hasher"))));

        String hashSHA256Hex = DigestHMAC.SHA256.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA256Hex, DigestHMAC.SHA256.digestHex("可以提供有状态的Hasher", key));

        String hashSHA512Hex = DigestHMAC.SHA512.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA512Hex, DigestHMAC.SHA512.digestHex("可以提供有状态的Hasher", key));
    }

    @Test
    public void testDigestMACBase64() throws Exception {
        String key = "192006250b4c09247ec02edce69f6a2d";

        String hashMD5Base64 = DigestHMAC.MD5.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashMD5Base64, DigestHMAC.MD5.digestBase64("可以提供有状态的Hasher", key));
        Mac HmacMD5 = Mac.getInstance("HmacMD5");
        HmacMD5.init(new SecretKeySpec(bytes(key), "HmacMD5"));
        assertEquals(hashMD5Base64, base64(HmacMD5.doFinal(bytes("可以提供有状态的Hasher"))));

        String hashSHA1Base64 = DigestHMAC.SHA1.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA1Base64, DigestHMAC.SHA1.digestBase64("可以提供有状态的Hasher", key));
        Mac HmacSHA1 = Mac.getInstance("HmacSHA1");
        HmacSHA1.init(new SecretKeySpec(bytes(key), "HmacSHA1"));
        assertEquals(hashSHA1Base64, base64(HmacSHA1.doFinal(bytes("可以提供有状态的Hasher"))));

        String hashSHA256Base64 = DigestHMAC.SHA256.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA256Base64, DigestHMAC.SHA256.digestBase64("可以提供有状态的Hasher", key));

        String hashSHA512Base64 = DigestHMAC.SHA512.digestBase64("可以提供有状态的Hasher", key);
        assertEquals(hashSHA512Base64, DigestHMAC.SHA512.digestBase64("可以提供有状态的Hasher", key));
    }
}
