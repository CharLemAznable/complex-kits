package com.github.charlemaznable.core.crypto;

import com.github.charlemaznable.core.codec.Base64;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Base64.unBase64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

public class RSA {

    private static final String RSAKEY = "RSA";

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";

    private static final int DEFAULT_KEY_SIZE = 1024;

    private RSA() {}

    /////////// key generate ///////////

    public static KeyPair generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }

    @SneakyThrows
    public static KeyPair generateKeyPair(int keysize) {
        val keyPairGenerator = KeyPairGenerator.getInstance(RSAKEY);
        keyPairGenerator.initialize(keysize);
        return keyPairGenerator.generateKeyPair();
    }

    public static PublicKey getPublicKey(KeyPair keyPair) {
        return keyPair.getPublic();
    }

    public static String getPublicKeyString(KeyPair keyPair) {
        return publicKeyString(getPublicKey(keyPair));
    }

    public static PrivateKey getPrivateKey(KeyPair keyPair) {
        return keyPair.getPrivate();
    }

    public static String getPrivateKeyString(KeyPair keyPair) {
        return privateKeyString(getPrivateKey(keyPair));
    }

    /////////// serialize/deserialize ///////////

    @SneakyThrows
    public static PublicKey publicKey(String publicKeyString) {
        return KeyFactory.getInstance(RSAKEY).generatePublic(
                new X509EncodedKeySpec(unBase64(publicKeyString)));
    }

    public static String publicKeyString(PublicKey publicKey) {
        return base64(publicKey.getEncoded(), Base64.Format.STANDARD);
    }

    @SneakyThrows
    public static PrivateKey privateKey(String privateKeyString) {
        return KeyFactory.getInstance(RSAKEY).generatePrivate(
                new PKCS8EncodedKeySpec(unBase64(privateKeyString)));
    }

    public static String privateKeyString(PrivateKey privateKey) {
        return base64(privateKey.getEncoded(), Base64.Format.STANDARD);
    }

    /////////// public/private key size ///////////

    @SneakyThrows
    public static int publicKeySize(PublicKey publicKey) {
        return KeyFactory.getInstance(RSAKEY).getKeySpec(publicKey,
                RSAPublicKeySpec.class).getModulus().toString(2).length();
    }

    @SneakyThrows
    public static int privateKeySize(PrivateKey privateKey) {
        return KeyFactory.getInstance(RSAKEY).getKeySpec(privateKey,
                RSAPrivateKeySpec.class).getModulus().toString(2).length();
    }

    /////////// en/de-crypt with key ///////////

    public static byte[] pubEncrypt(String plainText, PublicKey publicKey) {
        return encrypt(publicKey, publicKeySize(publicKey), plainText);
    }

    public static String prvDecrypt(byte[] cipherBytes, PrivateKey privateKey) {
        return decrypt(privateKey, privateKeySize(privateKey), cipherBytes);
    }

    public static byte[] prvEncrypt(String plainText, PrivateKey privateKey) {
        return encrypt(privateKey, privateKeySize(privateKey), plainText);
    }

    public static String pubDecrypt(byte[] cipherBytes, PublicKey publicKey) {
        return decrypt(publicKey, publicKeySize(publicKey), cipherBytes);
    }

    /////////// en/de-crypt with plainText ///////////

    public static byte[] encrypt(Key key, int keySize, String plainText) {
        return encryptByBlock(key, keySize, bytes(plainText));
    }

    public static String decrypt(Key key, int keySize, byte[] cipherBytes) {
        return string(decryptByBlock(key, keySize, cipherBytes));
    }

    /////////// private methods ///////////

    @SneakyThrows
    private static byte[] encryptByBlock(Key key, int keySize, byte[] data) {
        return cryptByBlock(ENCRYPT_MODE, key, keySize, data);
    }

    @SneakyThrows
    private static byte[] decryptByBlock(Key key, int keySize, byte[] data) {
        return cryptByBlock(DECRYPT_MODE, key, keySize, data);
    }

    private static byte[] cryptByBlock(int mode, Key key, int keySize, byte[] data) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, IOException {
        val cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(mode, key);
        val maxBlock = keySize / 8 - (ENCRYPT_MODE == mode ? 11 : 0);
        val inputLen = data.length;
        val out = new ByteArrayOutputStream();
        var offSet = 0;
        byte[] cache;
        var i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlock) {
                cache = cipher.doFinal(data, offSet, maxBlock);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlock;
        }
        val decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }
}
