package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Math.gcd;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathTest {

    @Test
    public void testGCD() {
        assertEquals(12, gcd(24, 60));
        assertEquals(29, gcd(319, 377));
        assertEquals(7, gcd(98, 63));
        assertEquals(42, gcd(756, 504, 630, 2226));
    }

    @Test
    public void testGCDLong() {
        assertEquals(12L, gcd(24L, 60L));
        assertEquals(29L, gcd(319L, 377L));
        assertEquals(7L, gcd(98L, 63L));
        assertEquals(42L, gcd(756L, 504L, 630L, 2226L));
    }
}
