package com.github.charlemaznable.lang.time;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.lang.time.Now.millis;
import static com.github.charlemaznable.lang.time.Now.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NowTest {

    @Test
    public void testNow() {
        assertEquals(19, now().length());
        assertEquals(23, millis().length());
    }
}
