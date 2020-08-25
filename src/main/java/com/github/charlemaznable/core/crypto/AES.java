package com.github.charlemaznable.core.crypto;

import lombok.SneakyThrows;
import lombok.val;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static java.lang.System.arraycopy;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

public final class AES {

    private static final String KEY_ALGORITHM = "AES";

    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    private AES() {}

    @SneakyThrows
    public static byte[] encrypt(String value, Key key) {
        val cipher = getInstance(CIPHER_ALGORITHM);
        cipher.init(ENCRYPT_MODE, key);
        return cipher.doFinal(bytes(value));
    }

    @SneakyThrows
    public static String decrypt(byte[] value, Key key) {
        val cipher = getInstance(CIPHER_ALGORITHM);
        cipher.init(DECRYPT_MODE, key);
        val decrypted = cipher.doFinal(value);
        return string(decrypted);
    }

    public static byte[] encrypt(String value, String keyString) {
        return encrypt(value, getKey(keyString));
    }

    public static String decrypt(byte[] value, String keyString) {
        return decrypt(value, getKey(keyString));
    }

    public static byte[] encrypt(String value, String keyString, int keySize) {
        return encrypt(value, getKey(keyString, keySize));
    }

    public static String decrypt(byte[] value, String keyString, int keySize) {
        return decrypt(value, getKey(keyString, keySize));
    }

    static Key getKey(String keyString) {
        return getKey(bytes(keyString));
    }

    static Key getKey(String keyString, int size) {
        return getKey(bytes(keyString), size);
    }

    static Key getKey(byte[] keyBytes) {
        /* Default 128bit */
        return getKey(keyBytes, 128);
    }

    static Key getKey(byte[] keyBytes, int size) {
        val dstBytes = new byte[size >> 3];

        if (keyBytes.length >= dstBytes.length) {
            arraycopy(keyBytes, 0, dstBytes, 0, dstBytes.length);
            return keySpec(dstBytes);
        }

        int pos = 0;
        while (pos + keyBytes.length < dstBytes.length) {
            arraycopy(keyBytes, 0, dstBytes, pos, keyBytes.length);
            pos += keyBytes.length;
        }
        arraycopy(keyBytes, 0, dstBytes, pos, dstBytes.length - pos);
        return keySpec(dstBytes);
    }

    static Key keySpec(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
}
