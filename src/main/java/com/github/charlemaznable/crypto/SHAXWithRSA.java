package com.github.charlemaznable.crypto;

import lombok.SneakyThrows;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import static com.github.charlemaznable.codec.Base64.base64;
import static com.github.charlemaznable.codec.Base64.unBase64;
import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Hex.hex;
import static com.github.charlemaznable.codec.Hex.unHex;
import static com.github.charlemaznable.crypto.RSA.privateKey;
import static com.github.charlemaznable.crypto.RSA.publicKey;
import static java.security.Signature.getInstance;

public enum SHAXWithRSA {

    SHA1WithRSA {
        @Override
        public String signAlgorithms() {
            return "SHA1WithRSA";
        }
    },
    SHA256WithRSA {
        @Override
        public String signAlgorithms() {
            return "SHA256WithRSA";
        }
    };

    public abstract String signAlgorithms();

    public byte[] sign(String plainText, String privateKey) {
        return sign(plainText, privateKey(privateKey));
    }

    @SneakyThrows
    public byte[] sign(String plainText, PrivateKey privateKey) {
        Signature signature = getInstance(signAlgorithms());
        signature.initSign(privateKey);
        signature.update(bytes(plainText));
        return signature.sign();
    }

    public boolean verify(String plainText, byte[] sign, String publicKey) {
        return verify(plainText, sign, publicKey(publicKey));
    }

    @SneakyThrows
    public boolean verify(String plainText, byte[] sign, PublicKey publicKey) {
        Signature signature = getInstance(signAlgorithms());
        signature.initVerify(publicKey);
        signature.update(bytes(plainText));
        return signature.verify(sign);
    }

    public String signBase64(String plainText, String privateKey) {
        return base64(sign(plainText, privateKey));
    }

    public String signBase64(String plainText, PrivateKey privateKey) {
        return base64(sign(plainText, privateKey));
    }

    public boolean verifyBase64(String plainText, String sign, String publicKey) {
        return verify(plainText, unBase64(sign), publicKey);
    }

    public boolean verifyBase64(String plainText, String sign, PublicKey publicKey) {
        return verify(plainText, unBase64(sign), publicKey);
    }

    public String signHex(String plainText, String privateKey) {
        return hex(sign(plainText, privateKey));
    }

    public String signHex(String plainText, PrivateKey privateKey) {
        return hex(sign(plainText, privateKey));
    }

    public boolean verifyHex(String plainText, String sign, String publicKey) {
        return verify(plainText, unHex(sign), publicKey);
    }

    public boolean verifyHex(String plainText, String sign, PublicKey publicKey) {
        return verify(plainText, unHex(sign), publicKey);
    }
}
