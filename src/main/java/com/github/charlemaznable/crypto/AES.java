package com.github.charlemaznable.crypto;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Bytes.string;
import static java.lang.System.arraycopy;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;
import static javax.crypto.Cipher.getInstance;

public class AES {

    private static final String KEY_ALGORITHM = "AES";

    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    @SneakyThrows
    public static byte[] encrypt(String value, Key key) {
        Cipher cipher = getInstance(CIPHER_ALGORITHM);
        cipher.init(ENCRYPT_MODE, key);
        return cipher.doFinal(bytes(value));
    }

    @SneakyThrows
    public static String decrypt(byte[] value, Key key) {
        Cipher cipher = getInstance(CIPHER_ALGORITHM);
        cipher.init(DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(value);
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

    private static Key getKey(String keyString) {
        /* Default 128bit */
        return getKey(keyString, 128);
    }

    private static Key getKey(String keyString, int size) {
        byte[] keyBytes = new byte[size >> 3];
        byte[] srcBytes = bytes(keyString);

        if (srcBytes.length >= keyBytes.length) {
            arraycopy(srcBytes, 0, keyBytes, 0, keyBytes.length);
            return keyFromString(keyBytes);
        }

        int pos = 0;
        while (pos + srcBytes.length < keyBytes.length) {
            arraycopy(srcBytes, 0, keyBytes, pos, srcBytes.length);
            pos += srcBytes.length;
        }
        arraycopy(srcBytes, 0, keyBytes, pos, keyBytes.length - pos);
        return keyFromString(keyBytes);
    }

    private static Key keyFromString(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
}
