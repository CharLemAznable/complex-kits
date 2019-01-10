package com.github.charlemaznable.codec;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestHMACTest {

    @Test
    public void testDigestMAC() throws Exception {
        String key = "192006250b4c09247ec02edce69f6a2d";

        String hashMD5 = DigestHMAC.MD5.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashMD5, DigestHMAC.MD5.digestHex("可以提供有状态的Hasher", key));
        Mac HmacMD5 = Mac.getInstance("HmacMD5");
        HmacMD5.init(new SecretKeySpec(bytes(key), "HmacMD5"));
        assertEquals(hashMD5, hex(HmacMD5.doFinal(bytes("可以提供有状态的Hasher"))));

        String hashSHA1 = DigestHMAC.SHA1.digestHex("可以提供有状态的Hasher", key);
        assertEquals(hashSHA1, DigestHMAC.SHA1.digestHex("可以提供有状态的Hasher", key));
        Mac HmacSHA1 = Mac.getInstance("HmacSHA1");
        HmacSHA1.init(new SecretKeySpec(bytes(key), "HmacSHA1"));
        assertEquals(hashSHA1, hex(HmacSHA1.doFinal(bytes("可以提供有状态的Hasher"))));
    }
}
