package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.lang.Math.gcd;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathTest {

    @Test
    public void testGCD() {
        assertEquals(12, gcd(24, 60));
        assertEquals(29, gcd(319, 377));
        assertEquals(7, gcd(98, 63));
        assertEquals(42, gcd(756, 504, 630, 2226));
    }
}
