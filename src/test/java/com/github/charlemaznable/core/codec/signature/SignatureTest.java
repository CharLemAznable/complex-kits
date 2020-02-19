package com.github.charlemaznable.core.codec.signature;

import com.github.charlemaznable.core.codec.Digest;
import com.github.charlemaznable.core.codec.DigestHMAC;
import com.github.charlemaznable.core.crypto.SHAXWithRSA;
import com.github.charlemaznable.core.lang.Mapp;
import lombok.val;
import lombok.var;
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
import static com.github.charlemaznable.core.crypto.RSA.generateKeyPair;
import static com.github.charlemaznable.core.crypto.RSA.getPrivateKeyString;
import static com.github.charlemaznable.core.crypto.RSA.getPublicKeyString;
import static com.github.charlemaznable.core.lang.Rand.randAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        var signature = signature(SOURCE);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(Digest.SHA256.digestBase64(PLAIN), signature.getValue());

        signature = signature(CUSTOM_KEY, SOURCE);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertEquals(Digest.SHA256.digestBase64(PLAIN), signature.getValue());

        signature = signature(SOURCE, new SignatureOptions().flatValue(false).keySortAsc(false));
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(Digest.SHA256.digestBase64(PLAIN_DESC), signature.getValue());
    }

    @Test
    public void testSignatureDigest() {
        var signature = signatureDigestBase64(SOURCE, Digest.SHA512);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(Digest.SHA512.digestBase64(PLAIN), signature.getValue());

        signature = signatureDigestBase64(CUSTOM_KEY, SOURCE, Digest.SHA512);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertEquals(Digest.SHA512.digestBase64(PLAIN), signature.getValue());

        signature = signatureDigestHex(SOURCE, Digest.SHA512);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(Digest.SHA512.digestHex(PLAIN), signature.getValue());

        signature = signatureDigestHex(CUSTOM_KEY, SOURCE, Digest.SHA512);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertEquals(Digest.SHA512.digestHex(PLAIN), signature.getValue());
    }

    @Test
    public void testSignatureDigestHMAC() {
        var signature = signatureDigestHMACBase64(SOURCE, DigestHMAC.SHA256, DIGEST_HMAC_KEY);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(DigestHMAC.SHA256.digestBase64(PLAIN, DIGEST_HMAC_KEY), signature.getValue());

        signature = signatureDigestHMACBase64(CUSTOM_KEY, SOURCE, DigestHMAC.SHA256, DIGEST_HMAC_KEY);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertEquals(DigestHMAC.SHA256.digestBase64(PLAIN, DIGEST_HMAC_KEY), signature.getValue());

        signature = signatureDigestHMACHex(SOURCE, DigestHMAC.SHA512, DIGEST_HMAC_KEY);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertEquals(DigestHMAC.SHA512.digestHex(PLAIN, DIGEST_HMAC_KEY), signature.getValue());

        signature = signatureDigestHMACHex(CUSTOM_KEY, SOURCE, DigestHMAC.SHA512, DIGEST_HMAC_KEY);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertEquals(DigestHMAC.SHA512.digestHex(PLAIN, DIGEST_HMAC_KEY), signature.getValue());
    }

    @Test
    public void testSignatureSHAWithRSA() {
        var signature = signatureSHAWithRSABase64(SOURCE, SHAXWithRSA.SHA1_WITH_RSA, RSA_PRV_KEY);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertTrue(SHAXWithRSA.SHA1_WITH_RSA.verifyBase64(PLAIN, signature.getValue(), RSA_PUB_KEY));

        signature = signatureSHAWithRSABase64(CUSTOM_KEY, SOURCE, SHAXWithRSA.SHA1_WITH_RSA, RSA_PRV_KEY);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertTrue(SHAXWithRSA.SHA1_WITH_RSA.verifyBase64(PLAIN, signature.getValue(), RSA_PUB_KEY));

        signature = signatureSHAWithRSAHex(SOURCE, SHAXWithRSA.SHA256_WITH_RSA, RSA_PRV_KEY);
        assertEquals(DEFAULT_KEY, signature.getKey());
        assertTrue(SHAXWithRSA.SHA256_WITH_RSA.verifyHex(PLAIN, signature.getValue(), RSA_PUB_KEY));

        signature = signatureSHAWithRSAHex(CUSTOM_KEY, SOURCE, SHAXWithRSA.SHA256_WITH_RSA, RSA_PRV_KEY);
        assertEquals(CUSTOM_KEY, signature.getKey());
        assertTrue(SHAXWithRSA.SHA256_WITH_RSA.verifyHex(PLAIN, signature.getValue(), RSA_PUB_KEY));
    }
}
