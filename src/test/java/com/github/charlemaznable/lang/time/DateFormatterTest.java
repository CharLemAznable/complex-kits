package com.github.charlemaznable.lang.time;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateFormatterTest {

    @Test
    public void testDateFormatter() {
        val formatter = new DateFormatter("yyyyMMddHHmmss");
        assertEquals("2006-01-02 15:04:05", formatter.transToFormat("20060102150405", "yyyy-MM-dd HH:mm:ss"));
        assertNull(formatter.transToFormat("200601021504", "yyyy-MM-dd HH:mm:ss"));
        assertEquals("20060102150405", formatter.transFromFormat("2006-01-02 15:04:05", "yyyy-MM-dd HH:mm:ss"));
        assertNull(formatter.transFromFormat("2006-01-02 15:04", "yyyy-MM-dd HH:mm:ss"));
    }

    @Test
    public void testDateFormatterCheck() {
        val formatter = new DateFormatter("yyyyMMddHHmmss");
        assertNull(formatter.checkFormatQuietly("2006-01-02 15:04:05"));
        assertEquals("20060102150405", formatter.checkFormatQuietly("20060102150405"));
        assertNull(formatter.checkFormat("2006-01-02 15:04:05"));
        assertEquals("20060102150405", formatter.checkFormat("20060102150405"));

        val formatter2 = new DateFormatter("yyyy-MM-dd HH:mm:ss");
        assertNull(formatter2.checkFormatQuietly("20060102150405"));
        assertEquals("2006-01-02 15:04:05", formatter2.checkFormatQuietly("2006-01-02 15:04:05"));
        assertThrows(ParseException.class, () -> formatter2.checkFormat("20060102150405"));
        assertEquals("2006-01-02 15:04:05", formatter2.checkFormat("2006-01-02 15:04:05"));
    }
}
