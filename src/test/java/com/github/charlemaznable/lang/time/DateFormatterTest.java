package com.github.charlemaznable.lang.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateFormatterTest {

    @Test
    public void testDateFormatter() {
        DateFormatter formatter = new DateFormatter("yyyyMMddHHmmss");
        assertEquals("2006-01-02 15:04:05", formatter.transToFormat("20060102150405", "yyyy-MM-dd HH:mm:ss"));
        assertNull(formatter.transToFormat("200601021504", "yyyy-MM-dd HH:mm:ss"));
        assertEquals("20060102150405", formatter.transFromFormat("2006-01-02 15:04:05", "yyyy-MM-dd HH:mm:ss"));
        assertNull(formatter.transFromFormat("2006-01-02 15:04", "yyyy-MM-dd HH:mm:ss"));
    }
}
