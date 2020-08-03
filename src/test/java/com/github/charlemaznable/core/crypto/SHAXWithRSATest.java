package com.github.charlemaznable.core.crypto;

import com.github.charlemaznable.core.lang.Rand;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.github.charlemaznable.core.crypto.RSA.generateKeyPair;
import static com.github.charlemaznable.core.crypto.RSA.getPrivateKeyString;
import static com.github.charlemaznable.core.crypto.RSA.getPublicKeyString;
import static com.github.charlemaznable.core.crypto.RSA.privateKey;
import static com.github.charlemaznable.core.crypto.RSA.publicKey;
import static com.github.charlemaznable.core.crypto.SHAXWithRSA.SHA1_WITH_RSA;
import static com.github.charlemaznable.core.crypto.SHAXWithRSA.SHA256_WITH_RSA;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SHAXWithRSATest {

    public final int TIMES = 10000;

    @Test
    public void testSHA1WithRSA() {
        val keyPair = generateKeyPair();
        val publicKeyString = getPublicKeyString(keyPair);
        val privateKeyString = getPrivateKeyString(keyPair);

        val plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";

        String signBase64 = SHA1_WITH_RSA.signBase64(plainText, privateKeyString);
        assertTrue(SHA1_WITH_RSA.verifyBase64(plainText, signBase64, publicKeyString));

        signBase64 = SHA1_WITH_RSA.signBase64(plainText, privateKey(privateKeyString));
        assertTrue(SHA1_WITH_RSA.verifyBase64(plainText, signBase64, publicKey(publicKeyString)));

        String signHex = SHA1_WITH_RSA.signHex(plainText, privateKeyString);
        assertTrue(SHA1_WITH_RSA.verifyHex(plainText, signHex, publicKeyString));

        signHex = SHA1_WITH_RSA.signHex(plainText, privateKey(privateKeyString));
        assertTrue(SHA1_WITH_RSA.verifyHex(plainText, signHex, publicKey(publicKeyString)));

        assertThrows(InvalidKeyException.class, () -> SHA1_WITH_RSA.sign(plainText, (PrivateKey) null));
        assertThrows(InvalidKeyException.class, () -> SHA1_WITH_RSA.verify(plainText, new byte[0], (PublicKey) null));
    }

    @Test
    public void testSHA256WithRSA() {
        val keyPair = generateKeyPair(2048);
        val publicKeyString = getPublicKeyString(keyPair);
        val privateKeyString = getPrivateKeyString(keyPair);

        val plainText = "{ mac=\"MAC Address\", appId=\"16位字符串\", signature=SHA1(\"appId=xxx&mac=yyy\") }";

        String signBase64 = SHA256_WITH_RSA.signBase64(plainText, privateKeyString);
        assertTrue(SHA256_WITH_RSA.verifyBase64(plainText, signBase64, publicKeyString));

        signBase64 = SHA256_WITH_RSA.signBase64(plainText, privateKey(privateKeyString));
        assertTrue(SHA256_WITH_RSA.verifyBase64(plainText, signBase64, publicKey(publicKeyString)));

        String signHex = SHA256_WITH_RSA.signHex(plainText, privateKeyString);
        assertTrue(SHA256_WITH_RSA.verifyHex(plainText, signHex, publicKeyString));

        signHex = SHA256_WITH_RSA.signHex(plainText, privateKey(privateKeyString));
        assertTrue(SHA256_WITH_RSA.verifyHex(plainText, signHex, publicKey(publicKeyString)));

        assertThrows(InvalidKeyException.class, () -> SHA256_WITH_RSA.sign(plainText, (PrivateKey) null));
        assertThrows(InvalidKeyException.class, () -> SHA256_WITH_RSA.verify(plainText, new byte[0], (PublicKey) null));
    }

    @Test
    public void testAlipayRSA2() {
        val plainText = "{\"a\":\"123\"}";
        val publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhstdg5kW1+THuJFThCSADOdvuASIgqS+U1COLcVDMqzAKWbBZmMH+RH16Q34TRSDJrMeeHpxFhhdwrnTc8wTelPsjSi0Peod4d4ZqePK3xxwyOkbvOBO53+gEV1BCku5omVt6S7Ji7HJmufagN9MIJ1gUdDFAUnuf0g8SQF+RcowXbQcbFCcWi5GezFt7O7LeWLFrnAXpWB/vdwFm3rhHdWAHDRKnnCet/nCrFx918ll6KgtsNFppvgHqQsAGoYLd0WgDV78NcZqFw7dEAW9ZXKlbs6afM8fKuPKEtRPhICZ+qQemtHYP5dYDjA7Dd0Tqd98Db250lPJQ42Tw2379QIDAQAB";
        val privateKeyString = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGy12DmRbX5Me4kVOEJIAM52+4BIiCpL5TUI4txUMyrMApZsFmYwf5EfXpDfhNFIMmsx54enEWGF3CudNzzBN6U+yNKLQ96h3h3hmp48rfHHDI6Ru84E7nf6ARXUEKS7miZW3pLsmLscma59qA30wgnWBR0MUBSe5/SDxJAX5FyjBdtBxsUJxaLkZ7MW3s7st5YsWucBelYH+93AWbeuEd1YAcNEqecJ63+cKsXH3XyWXoqC2w0Wmm+AepCwAahgt3RaANXvw1xmoXDt0QBb1lcqVuzpp8zx8q48oS1E+EgJn6pB6a0dg/l1gOMDsN3ROp33wNvbnSU8lDjZPDbfv1AgMBAAECggEAeRo86f6gh1cEGM54OkCQywcjWDC6dKWOWUwGcRjJRr3t50RqfZJ7l0qLpNboPlgvfJVwys02zKMXmdtaHq860KyV2jy3suij8EmaxQfjPJBQxDCquwbL3TDMns0pohZmsYBvPxDKAre4aXPk2+PCgYri/p6SYJYLsE812mF9ETVHC3Q2YGiWKjYe5eAeAqLRhZAV+JElNZBxpM7C2kYDlr/EXCtl+auxR7HDc6CHToLLCh3EDaowUOtXpXEIK9H2ah7mf0UHOE/bkzGarrAbuTv4oFMJdmU73ja6dnIvT+vmy8/pVE4cw+MqbwdSRBPRbiowTVzHq5fAOjTX9m1IAQKBgQDLpJmG5nAx6HZJaj67DcdYuIzyq39IwBpDibEPstnVP6ySHzZZ61q7peLX3m1SkCBc9Kx5JxfgPBwteMNq73kzThj+yZwZAXb3nyJCQEO3nW5hmgBiMsLoSFSdz78NV3TnobpKs5Of6uvm4frLzwe1+Ot5E6URyEpBo1O8U8engwKBgQCpc0fuwUoyJ16gj9bbEYQFf9q2l1yk2Vqn7/389w9i1qHa8e6gZjVsB6Q5qv7GERPGq7x6eIy+3WHMM9v5xe2Hwq6BaWpfUfvh5g7o3MoMAGaNcodcJNYDZEmAGbTQ6iUPC1GqkC9CRRZfAbJeONML37hU0obBmlFoqS2E4f39JwKBgE4O9O55rC3lPlobyJnoyS3f598pD6wyPp7+y4kJ9GfCdYK5RhsnlOryxkYas2r/ZfhbrTTvYD6Kq/5eGQIHczzQRdJ8M0J+hjyamgYtHgfm56Wv40/Ax2dEOnMXa9NQX7ZeDsuNlBurb+tbWcf/vqPpG1GYzyBG1vfcBD/fGVvDAoGAKMo3ZqHg2nHftYbqkZGF1k0QuSN2ibhIumRrH5nZelFwZOQYLURtV7P/NGKVMdiqigiNNm4Oc2OXRzC0o0uiAiw7Favqj0eM+rQGoseDW6A9D4+iRewYGHjruIp3nFLA2P5Aim5q8ejvma8u4L+NDi6skL6c8b3UGv2el35PRcECgYEAuF+Tz2Iay0CqGynxyJWqEXbHaF2Kw+rSIgoQO5vlRYeuuR0jaZUH0wRb760D4es/No5Xbuj+70LAd0NnqnsCu6Mj26xy2ZjG1bJQRW9sK3KprNLWQxyiRc/8gQtcxuVpNqgnWupGbflzXtp10NqsgfCCqfoebwrFseZRWdoqH6Y=";

        val signBase64 = SHA256_WITH_RSA.signBase64(plainText, privateKeyString);
        assertTrue(SHA256_WITH_RSA.verifyBase64(plainText, signBase64, publicKeyString));
    }

    public void batchRun(SHAXWithRSA shax, int times) {
        val rand = Rand.randAlphanumeric(100);
        val keyPair = generateKeyPair();
        val publicKeyString = getPublicKeyString(keyPair);
        val privateKeyString = getPrivateKeyString(keyPair);

        for (int i = 0; i < times; ++i) {
            val plainText = rand + i;
            val signBase64 = shax.signBase64(plainText, privateKeyString);
            assertTrue(shax.verifyBase64(plainText, signBase64, publicKeyString));
        }
    }

    @SneakyThrows
    public void routineRun(SHAXWithRSA shax, int threads) {
        val service = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            service[i] = new Thread(() -> batchRun(shax, TIMES));
            service[i].start();
        }

        for (int i = 0; i < threads; i++) {
            service[i].join();
        }
    }

    @Test
    public void testSHXWithRSABatch() {
        routineRun(SHA1_WITH_RSA, getRuntime().availableProcessors() + 1);
        routineRun(SHA256_WITH_RSA, getRuntime().availableProcessors() + 1);
    }
}
