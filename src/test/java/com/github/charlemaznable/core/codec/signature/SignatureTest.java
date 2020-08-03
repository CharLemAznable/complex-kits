package com.github.charlemaznable.core.codec.signature;

import com.github.charlemaznable.core.codec.Digest;
import com.github.charlemaznable.core.codec.DigestHMAC;
import com.github.charlemaznable.core.crypto.SHAXWithRSA;
import com.github.charlemaznable.core.lang.Mapp;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.signature.Signature.signature;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureDigestBase64;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureDigestHMACBase64;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureDigestHMACHex;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureDigestHex;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureSHAWithRSABase64;
import static com.github.charlemaznable.core.codec.signature.Signature.signatureSHAWithRSAHex;
import static com.github.charlemaznable.core.codec.signature.Signature.verify;
import static com.github.charlemaznable.core.codec.signature.Signature.verifyDigestBase64;
import static com.github.charlemaznable.core.codec.signature.Signature.verifyDigestHMACBase64;
import static com.github.charlemaznable.core.codec.signature.Signature.verifyDigestHMACHex;
import static com.github.charlemaznable.core.codec.signature.Signature.verifyDigestHex;
import static com.github.charlemaznable.core.codec.signature.Signature.verifySHAWithRSABase64;
import static com.github.charlemaznable.core.codec.signature.Signature.verifySHAWithRSAHex;
import static com.github.charlemaznable.core.crypto.RSA.generateKeyPair;
import static com.github.charlemaznable.core.crypto.RSA.getPrivateKeyString;
import static com.github.charlemaznable.core.crypto.RSA.getPublicKeyString;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Rand.randAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureTest {

    private static final Map<String, Object> SOURCE = Mapp.of("AAA", "aaa", "BBB", "bbb");
    private static final String PLAIN = "AAA=aaa&BBB=bbb";
    private static final String PLAIN_DESC = "BBB=bbb&AAA=aaa";
    private static final byte[] DIGEST_HMAC_KEY = bytes(randAlphanumeric(32));
    private static final String RSA_PUB_KEY;
    private static final String RSA_PRV_KEY;
    private static final String DEFAULT_KEY = "signature";
    private static final String CUSTOM_KEY = "sign";

    static {
        val keyPair = generateKeyPair();
        RSA_PUB_KEY = getPublicKeyString(keyPair);
        RSA_PRV_KEY = getPrivateKeyString(keyPair);
    }

    @Test
    public void testSignature() {
        Signature signature = signature(SOURCE);
        Map<String, Object> verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verify(verifySource));

        signature = signature(CUSTOM_KEY, SOURCE);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verify(CUSTOM_KEY, verifySource));

        val falseOptions = new SignatureOptions().flatValue(false).keySortAsc(false);
        signature = signature(SOURCE, falseOptions);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verify(verifySource, falseOptions));
    }

    @Test
    public void testSignatureDigest() {
        Signature signature = signatureDigestBase64(SOURCE, Digest.SHA512);
        Map<String, Object> verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestBase64(verifySource, Digest.SHA512));

        signature = signatureDigestBase64(CUSTOM_KEY, SOURCE, Digest.SHA512);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestBase64(CUSTOM_KEY, verifySource, Digest.SHA512));

        signature = signatureDigestHex(SOURCE, Digest.SHA512);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHex(verifySource, Digest.SHA512));

        signature = signatureDigestHex(CUSTOM_KEY, SOURCE, Digest.SHA512);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHex(CUSTOM_KEY, verifySource, Digest.SHA512));
    }

    @Test
    public void testSignatureDigestHMAC() {
        Signature signature = signatureDigestHMACBase64(SOURCE, DigestHMAC.SHA256, DIGEST_HMAC_KEY);
        Map<String, Object> verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHMACBase64(verifySource, DigestHMAC.SHA256, DIGEST_HMAC_KEY));

        signature = signatureDigestHMACBase64(CUSTOM_KEY, SOURCE, DigestHMAC.SHA256, DIGEST_HMAC_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHMACBase64(CUSTOM_KEY, verifySource, DigestHMAC.SHA256, DIGEST_HMAC_KEY));

        signature = signatureDigestHMACHex(SOURCE, DigestHMAC.SHA512, DIGEST_HMAC_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHMACHex(verifySource, DigestHMAC.SHA512, DIGEST_HMAC_KEY));

        signature = signatureDigestHMACHex(CUSTOM_KEY, SOURCE, DigestHMAC.SHA512, DIGEST_HMAC_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifyDigestHMACHex(CUSTOM_KEY, verifySource, DigestHMAC.SHA512, DIGEST_HMAC_KEY));
    }

    @Test
    public void testSignatureSHAWithRSA() {
        Signature signature = signatureSHAWithRSABase64(SOURCE, SHAXWithRSA.SHA1_WITH_RSA, RSA_PRV_KEY);
        Map<String, Object> verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifySHAWithRSABase64(verifySource, SHAXWithRSA.SHA1_WITH_RSA, RSA_PUB_KEY));

        signature = signatureSHAWithRSABase64(CUSTOM_KEY, SOURCE, SHAXWithRSA.SHA1_WITH_RSA, RSA_PRV_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifySHAWithRSABase64(CUSTOM_KEY, verifySource, SHAXWithRSA.SHA1_WITH_RSA, RSA_PUB_KEY));

        signature = signatureSHAWithRSAHex(SOURCE, SHAXWithRSA.SHA256_WITH_RSA, RSA_PRV_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifySHAWithRSAHex(verifySource, SHAXWithRSA.SHA256_WITH_RSA, RSA_PUB_KEY));

        signature = signatureSHAWithRSAHex(CUSTOM_KEY, SOURCE, SHAXWithRSA.SHA256_WITH_RSA, RSA_PRV_KEY);
        verifySource = newHashMap(SOURCE);
        verifySource.put(signature.getKey(), signature.getValue());
        assertTrue(verifySHAWithRSAHex(CUSTOM_KEY, verifySource, SHAXWithRSA.SHA256_WITH_RSA, RSA_PUB_KEY));
    }
}
