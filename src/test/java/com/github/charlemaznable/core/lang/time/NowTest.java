package com.github.charlemaznable.core.lang.time;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.time.Now.millis;
import static com.github.charlemaznable.core.lang.time.Now.now;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NowTest {

    @Test
    public void testNow() {
        assertEquals(19, now().length());
        assertEquals(23, millis().length());

        val dateFormatter1 = new DateFormatter("yyyy-MM-dd HH:mm:ss");
        assertEquals(19, now(dateFormatter1).length());
        val dateFormatter2 = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS");
        assertEquals(23, now(dateFormatter2).length());
    }
}
