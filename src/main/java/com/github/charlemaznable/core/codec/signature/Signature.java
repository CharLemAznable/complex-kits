package com.github.charlemaznable.core.codec.signature;

import com.github.charlemaznable.core.codec.Digest;
import com.github.charlemaznable.core.codec.DigestHMAC;
import com.github.charlemaznable.core.codec.Json;
import com.github.charlemaznable.core.crypto.SHAXWithRSA;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Signature {

    private String key;
    private String value;

    public static Signature signature(Map<String, Object> source) {
        return signature(source, new SignatureOptions());
    }

    public static Signature signature(String key, Map<String, Object> source) {
        return signature(source, new SignatureOptions().key(key));
    }

    public static Signature signatureDigestBase64(Map<String, Object> source, Digest digest) {
        return signature(source, new SignatureOptions().signAlgorithm(digest::digestBase64));
    }

    public static Signature signatureDigestBase64(String key, Map<String, Object> source, Digest digest) {
        return signature(source, new SignatureOptions().key(key).signAlgorithm(digest::digestBase64));
    }

    public static Signature signatureDigestHex(Map<String, Object> source, Digest digest) {
        return signature(source, new SignatureOptions().signAlgorithm(digest::digestHex));
    }

    public static Signature signatureDigestHex(String key, Map<String, Object> source, Digest digest) {
        return signature(source, new SignatureOptions().key(key).signAlgorithm(digest::digestHex));
    }

    public static Signature signatureDigestHMACBase64
            (Map<String, Object> source, DigestHMAC digestHMAC, byte[] digestKey) {
        return signature(source, new SignatureOptions()
                .signAlgorithm(s -> digestHMAC.digestBase64(s, digestKey)));
    }

    public static Signature signatureDigestHMACBase64
            (String key, Map<String, Object> source, DigestHMAC digestHMAC, byte[] digestKey) {
        return signature(source, new SignatureOptions().key(key)
                .signAlgorithm(s -> digestHMAC.digestBase64(s, digestKey)));
    }

    public static Signature signatureDigestHMACHex
            (Map<String, Object> source, DigestHMAC digestHMAC, byte[] digestKey) {
        return signature(source, new SignatureOptions()
                .signAlgorithm(s -> digestHMAC.digestHex(s, digestKey)));
    }

    public static Signature signatureDigestHMACHex
            (String key, Map<String, Object> source, DigestHMAC digestHMAC, byte[] digestKey) {
        return signature(source, new SignatureOptions().key(key)
                .signAlgorithm(s -> digestHMAC.digestHex(s, digestKey)));
    }

    public static Signature signatureSHAWithRSABase64
            (Map<String, Object> source, SHAXWithRSA shaxWithRSA, String privateKey) {
        return signature(source, new SignatureOptions()
                .signAlgorithm(s -> shaxWithRSA.signBase64(s, privateKey)));
    }

    public static Signature signatureSHAWithRSABase64
            (String key, Map<String, Object> source, SHAXWithRSA shaxWithRSA, String privateKey) {
        return signature(source, new SignatureOptions().key(key)
                .signAlgorithm(s -> shaxWithRSA.signBase64(s, privateKey)));
    }

    public static Signature signatureSHAWithRSAHex
            (Map<String, Object> source, SHAXWithRSA shaxWithRSA, String privateKey) {
        return signature(source, new SignatureOptions()
                .signAlgorithm(s -> shaxWithRSA.signHex(s, privateKey)));
    }

    public static Signature signatureSHAWithRSAHex
            (String key, Map<String, Object> source, SHAXWithRSA shaxWithRSA, String privateKey) {
        return signature(source, new SignatureOptions().key(key)
                .signAlgorithm(s -> shaxWithRSA.signHex(s, privateKey)));
    }

    public static Signature signature(Map<String, Object> source, SignatureOptions options) {
        Map<String, String> flatMap;
        if (options.flatValue()) {
            flatMap = Json.descFlat(source);
        } else {
            flatMap = new HashMap<>();
            source.forEach((k, v) -> flatMap.put(
                    k, notNullThen(v, Object::toString)));
        }
        Map<String, String> tempMap = options.keySortAsc()
                ? new TreeMap<>(flatMap) : new TreeMap<>(flatMap).descendingMap();
        val plain = tempMap.entrySet().stream()
                .filter(options.entryFilter())
                .map(options.entryMapper())
                .collect(Collectors.joining(options.entrySeparator()));
        return new Signature(options.key(), options.signAlgorithm().apply(plain));
    }
}
