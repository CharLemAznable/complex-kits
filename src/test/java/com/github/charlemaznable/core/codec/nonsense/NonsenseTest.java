package com.github.charlemaznable.core.codec.nonsense;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.nonsense.Nonsense.nonsense;
import static com.github.charlemaznable.core.codec.nonsense.Nonsense.nonsenseAscii;
import static com.github.charlemaznable.core.codec.nonsense.Nonsense.nonsenseLetters;
import static com.github.charlemaznable.core.codec.nonsense.Nonsense.nonsenseNumbers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NonsenseTest {

    private static final String DEFAULT_KEY = "nonsense";
    private static final String CUSTOM_KEY = "nonce";

    @Test
    public void testNonsense() {
        Nonsense nonsense = nonsense();
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z0-9]{16}"));

        nonsense = nonsense(32);
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z0-9]{32}"));

        nonsense = nonsense(CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z0-9]{16}"));

        nonsense = nonsense(32, CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z0-9]{32}"));
    }

    @Test
    public void testNonsenseAscii() {
        Nonsense nonsense = nonsenseAscii();
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[\\x20-\\xff]{16}"));

        nonsense = nonsenseAscii(32);
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[\\x20-\\xff]{32}"));

        nonsense = nonsenseAscii(CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[\\x20-\\xff]{16}"));

        nonsense = nonsenseAscii(32, CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[\\x20-\\xff]{32}"));
    }

    @Test
    public void testNonsenseNumbers() {
        Nonsense nonsense = nonsenseNumbers();
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[0-9]{16}"));

        nonsense = nonsenseNumbers(32);
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[0-9]{32}"));

        nonsense = nonsenseNumbers(CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[0-9]{16}"));

        nonsense = nonsenseNumbers(32, CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[0-9]{32}"));
    }

    @Test
    public void testNonsenseLetters() {
        Nonsense nonsense = nonsenseLetters();
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z]{16}"));

        nonsense = nonsenseLetters(32);
        assertEquals(DEFAULT_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z]{32}"));

        nonsense = nonsenseLetters(CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z]{16}"));

        nonsense = nonsenseLetters(32, CUSTOM_KEY);
        assertEquals(CUSTOM_KEY, nonsense.getKey());
        assertTrue(nonsense.getValue().matches("[A-Za-z]{32}"));
    }
}
