package com.github.charlemaznable.lang.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NowTest {

    @Test
    public void testNow() {
        assertEquals(19, Now.now().length());
        assertEquals(23, Now.millis().length());
    }
}
