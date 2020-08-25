package com.github.charlemaznable.core.crypto;

import com.github.charlemaznable.core.crypto.FPE.AlphabetDomain;
import com.github.charlemaznable.core.crypto.FPE.AlphabetDomains;
import com.github.charlemaznable.core.crypto.FPE.GenericAlphabet;
import com.github.charlemaznable.core.lang.Rand;
import com.idealista.fpe.builder.FormatPreservingEncryptionBuilder;
import com.idealista.fpe.config.Domain;
import com.idealista.fpe.config.LengthRange;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.crypto.FPE.AlphabetDomains.ALPHANUMERIC;
import static com.github.charlemaznable.core.crypto.FPE.AlphabetDomains.LOWER_LETTERS;
import static java.lang.Runtime.getRuntime;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FPETest {

    public final int TIMES = 10000;
    public final AtomicInteger success = new AtomicInteger(0);

    @Test
    public void testFPEDefault() {
        val formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDefaultDomain()
                .withDefaultPseudoRandomFunction(bytes("0123456789012345"))
                .withDefaultLengthRange()
                .build();
        val fpe = FPE.ff1()
                .withDomain(LOWER_LETTERS)
                .withPseudoRandomKey(bytes("0123456789"))
                .build();

        val plainText = "aaaa";
        val tweakText = Rand.randAlphanumeric(4);

        val encrypt1 = formatPreservingEncryption.encrypt(plainText, bytes(tweakText));
        val encrypt2 = fpe.encrypt(plainText, tweakText);
        assertEquals(encrypt1, encrypt2);

        val decrypt1 = formatPreservingEncryption.decrypt(encrypt1, bytes(tweakText));
        val decrypt2 = fpe.decrypt(encrypt2, tweakText);
        assertEquals(plainText, decrypt1);
        assertEquals(plainText, decrypt1);
    }

    @Test
    public void testFPEDomain() {
        val formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(new AlphabetDomain(new GenericAlphabet("abcdefghij".toCharArray())))
                .withDefaultPseudoRandomFunction(bytes("012345678901234567890123"))
                .withDefaultLengthRange()
                .build();
        val fpe = FPE.ff1()
                .withDomain("abcdefghij")
                .withDomain((Domain) null)
                .withDomain((AlphabetDomains) null)
                .withDomain((String) null)
                .withDomain((char[]) null)
                .withPseudoRandomKey(bytes("0123456789"), 192)
                .build();

        val plainText = "aaaa";
        val tweakText = Rand.randAlphanumeric(4);

        val encrypt1 = formatPreservingEncryption.encrypt(plainText, bytes(tweakText));
        val encrypt2 = fpe.encrypt(plainText, tweakText);
        assertEquals(encrypt1, encrypt2);

        val decrypt1 = formatPreservingEncryption.decrypt(encrypt1, bytes(tweakText));
        val decrypt2 = fpe.decrypt(encrypt2, tweakText);
        assertEquals(plainText, decrypt1);
        assertEquals(plainText, decrypt1);
    }

    @Test
    public void testFPEPseudoRandomFunction() {
        val formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(ALPHANUMERIC.domain())
                .withDefaultPseudoRandomFunction(bytes("0123456789012345"))
                .withDefaultLengthRange()
                .build();
        val fpe = FPE.ff1()
                .withPseudoRandomKey("0123456789")
                .withPseudoRandomFunction(null)
                .withPseudoRandomKey((String) null)
                .withPseudoRandomKey((byte[]) null)
                .build();

        val plainText = "aaaa";
        val tweakText = Rand.randAlphanumeric(4);

        val encrypt1 = formatPreservingEncryption.encrypt(plainText, bytes(tweakText));
        val encrypt2 = fpe.encrypt(plainText, tweakText);
        assertEquals(encrypt1, encrypt2);

        val decrypt1 = formatPreservingEncryption.decrypt(encrypt1, bytes(tweakText));
        val decrypt2 = fpe.decrypt(encrypt2, tweakText);
        assertEquals(plainText, decrypt1);
        assertEquals(plainText, decrypt1);
    }

    @Test
    public void testFPEPseudoRandomFunction2() {
        val formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(ALPHANUMERIC.domain())
                .withDefaultPseudoRandomFunction(bytes("01234567890123456789012345678901"))
                .withDefaultLengthRange()
                .build();
        val fpe = FPE.ff1()
                .withPseudoRandomKey("0123456789", 256)
                .withPseudoRandomKey((String) null, 256)
                .withPseudoRandomKey((byte[]) null, 256)
                .build();

        val plainText = "aaaa";
        val tweakText = Rand.randAlphanumeric(4);

        val encrypt1 = formatPreservingEncryption.encrypt(plainText, bytes(tweakText));
        val encrypt2 = fpe.encrypt(plainText, tweakText);
        assertEquals(encrypt1, encrypt2);

        val decrypt1 = formatPreservingEncryption.decrypt(encrypt1, bytes(tweakText));
        val decrypt2 = fpe.decrypt(encrypt2, tweakText);
        assertEquals(plainText, decrypt1);
        assertEquals(plainText, decrypt1);
    }

    @Test
    public void testFPELengthRange() {
        val formatPreservingEncryption = FormatPreservingEncryptionBuilder
                .ff1Implementation()
                .withDomain(new AlphabetDomain(new GenericAlphabet("abcde".toCharArray())))
                .withDefaultPseudoRandomFunction(bytes("0123456789012345"))
                .withLengthRange(new LengthRange(3, Integer.MAX_VALUE))
                .build();
        val fpe = FPE.ff1()
                .withDomain("abcde")
                .withPseudoRandomKey("0123456789")
                .withLengthRange(3, null)
                .withLengthRange(null)
                .build();

        val plainText = "aaaa";
        val tweakText = Rand.randAlphanumeric(4);

        val encrypt1 = formatPreservingEncryption.encrypt(plainText, bytes(tweakText));
        val encrypt2 = fpe.encrypt(plainText, tweakText);
        assertEquals(encrypt1, encrypt2);

        val decrypt1 = formatPreservingEncryption.decrypt(encrypt1, bytes(tweakText));
        val decrypt2 = fpe.decrypt(encrypt2, tweakText);
        assertEquals(plainText, decrypt1);
        assertEquals(plainText, decrypt1);
    }

    public void batchRun(FPE fpe, int times) {
        val plainText = Rand.randAlphanumeric(100);
        val tweakText = Rand.randAlphanumeric(4);

        for (int i = 0; i < times; ++i) {
            val plain = plainText + i;
            val enc = fpe.encrypt(plain, tweakText);
            val dec = fpe.decrypt(enc, tweakText);
            assertEquals(plain, dec);
            success.incrementAndGet();
        }
    }

    @SneakyThrows
    public void routineRun(int threads) {
        val fpe = FPE.ff1()
                .withPseudoRandomKey("0123456789")
                .build();
        val service = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            service[i] = new Thread(() -> batchRun(fpe, TIMES));
            service[i].start();
        }

        for (int i = 0; i < threads; i++) {
            service[i].join();
        }
    }

    @Test
    public void testFPEBatch() {
        val threads = getRuntime().availableProcessors() + 1;
        routineRun(threads);
        assertEquals(threads * TIMES, success.get());
    }
}
