package com.github.charlemaznable.core.codec.signature;

import com.github.charlemaznable.core.codec.Digest;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureOptionsTest {

    @Test
    public void testSignatureOptions() {
        val options = new SignatureOptions();

        assertEquals("signature", options.key());
        assertTrue(options.flatValue());
        assertTrue(options.keySortAsc());
        assertNotNull(options.entryFilter());
        assertNotNull(options.entryMapper());
        assertEquals("&", options.entrySeparator());
        assertEquals(Digest.SHA256.digestBase64("Hello"), options.signAlgorithm().apply("Hello"));

        options.key("sign").flatValue(false).keySortAsc(false)
                .entryFilter(e -> true)
                .entryMapper(e -> e.getKey() + ":" + e.getValue())
                .entrySeparator("$")
                .signAlgorithm(Digest.SHA512::digestHex);

        assertEquals("sign", options.key());
        assertFalse(options.flatValue());
        assertFalse(options.keySortAsc());
        assertNotNull(options.entryFilter());
        assertNotNull(options.entryMapper());
        assertEquals("$", options.entrySeparator());
        assertEquals(Digest.SHA512.digestHex("World"), options.signAlgorithm().apply("World"));
    }
}
