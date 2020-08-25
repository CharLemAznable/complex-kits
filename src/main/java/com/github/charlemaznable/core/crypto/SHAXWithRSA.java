package com.github.charlemaznable.core.crypto;

import lombok.SneakyThrows;
import lombok.val;

import java.security.PrivateKey;
import java.security.PublicKey;

import static com.github.charlemaznable.core.codec.Base64.base64;
import static com.github.charlemaznable.core.codec.Base64.unBase64;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Hex.hex;
import static com.github.charlemaznable.core.codec.Hex.unHex;
import static com.github.charlemaznable.core.crypto.RSA.privateKey;
import static com.github.charlemaznable.core.crypto.RSA.publicKey;
import static java.security.Signature.getInstance;

public enum SHAXWithRSA {

    SHA1_WITH_RSA {
        @Override
        protected final String signAlgorithms() {
            return "SHA1WithRSA";
        }
    },
    SHA256_WITH_RSA {
        @Override
        protected final String signAlgorithms() {
            return "SHA256WithRSA";
        }
    },;

    protected abstract String signAlgorithms();

    public final byte[] sign(String plainText, String privateKey) {
        return sign(plainText, privateKey(privateKey));
    }

    @SneakyThrows
    public final byte[] sign(String plainText, PrivateKey privateKey) {
        val signature = getInstance(signAlgorithms());
        signature.initSign(privateKey);
        signature.update(bytes(plainText));
        return signature.sign();
    }

    public final boolean verify(String plainText, byte[] sign, String publicKey) {
        return verify(plainText, sign, publicKey(publicKey));
    }

    @SneakyThrows
    public final boolean verify(String plainText, byte[] sign, PublicKey publicKey) {
        val signature = getInstance(signAlgorithms());
        signature.initVerify(publicKey);
        signature.update(bytes(plainText));
        return signature.verify(sign);
    }

    public final String signBase64(String plainText, String privateKey) {
        return base64(sign(plainText, privateKey));
    }

    public final String signBase64(String plainText, PrivateKey privateKey) {
        return base64(sign(plainText, privateKey));
    }

    public final boolean verifyBase64(String plainText, String sign, String publicKey) {
        return verify(plainText, unBase64(sign), publicKey);
    }

    public final boolean verifyBase64(String plainText, String sign, PublicKey publicKey) {
        return verify(plainText, unBase64(sign), publicKey);
    }

    public final String signHex(String plainText, String privateKey) {
        return hex(sign(plainText, privateKey));
    }

    public final String signHex(String plainText, PrivateKey privateKey) {
        return hex(sign(plainText, privateKey));
    }

    public final boolean verifyHex(String plainText, String sign, String publicKey) {
        return verify(plainText, unHex(sign), publicKey);
    }

    public final boolean verifyHex(String plainText, String sign, PublicKey publicKey) {
        return verify(plainText, unHex(sign), publicKey);
    }
}
