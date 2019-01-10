package com.github.charlemaznable.codec;


import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DigestTest {

    @Test
    @Deprecated
    public void testDigest() throws NoSuchAlgorithmException {
        String hashMD5 = Digest.MD5.digestHex("可以提供有状态的Hasher");
        assertEquals(hashMD5, Digest.MD5.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashMD5, hex(MessageDigest.getInstance("MD5").digest(bytes("可以提供有状态的Hasher"))));

        String hashSHA1 = Digest.SHA1.digestHex("可以提供有状态的Hasher");
        assertEquals(hashSHA1, Digest.SHA1.digestHex("可以提供有状态的Hasher"));
        assertEquals(hashSHA1, hex(MessageDigest.getInstance("SHA1").digest(bytes("可以提供有状态的Hasher"))));
    }
}
