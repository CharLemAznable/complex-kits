package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathTest {

    @Test
    public void testGCD() {
        assertEquals(12, Math.gcd(24, 60));
        assertEquals(29, Math.gcd(319, 377));
        assertEquals(7, Math.gcd(98, 63));
        assertEquals(42, Math.gcd(756, 504, 630, 2226));
    }
}
