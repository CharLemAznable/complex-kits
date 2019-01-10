package com.github.charlemaznable.crypto;


import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.github.charlemaznable.crypto.RSA.generateKeyPair;
import static com.github.charlemaznable.crypto.RSA.getPrivateKeyString;
import static com.github.charlemaznable.crypto.RSA.getPublicKeyString;
import static com.github.charlemaznable.crypto.RSA.privateKey;
import static com.github.charlemaznable.crypto.RSA.prvDecrypt;
import static com.github.charlemaznable.crypto.RSA.prvEncrypt;
import static com.github.charlemaznable.crypto.RSA.pubDecrypt;
import static com.github.charlemaznable.crypto.RSA.pubEncrypt;
import static com.github.charlemaznable.crypto.RSA.publicKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RSATest {

    @Test
    public void testRSA() {
        String plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        KeyPair keyPair = generateKeyPair();
        String publicKeyString = getPublicKeyString(keyPair);
        String privateKeyString = getPrivateKeyString(keyPair);

        PublicKey publicKey = publicKey(publicKeyString);
        PrivateKey privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }

    @Test
    public void testRSA2() {
        String plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";
        KeyPair keyPair = generateKeyPair(2048);
        String publicKeyString = getPublicKeyString(keyPair);
        String privateKeyString = getPrivateKeyString(keyPair);

        PublicKey publicKey = publicKey(publicKeyString);
        PrivateKey privateKey = privateKey(privateKeyString);

        assertEquals(plainText, prvDecrypt(pubEncrypt(plainText, publicKey), privateKey));
        assertEquals(plainText, pubDecrypt(prvEncrypt(plainText, privateKey), publicKey));
    }
}
