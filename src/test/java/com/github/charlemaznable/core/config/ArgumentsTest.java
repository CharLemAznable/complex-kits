package com.github.charlemaznable.core.config;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ArgumentsTest {

    @Test
    public void testArguments() {
        Arguments.initial("--key1=value1", "enabled");

        val arguments = new Arguments();
        assertTrue(arguments.exists("key1"));
        assertFalse(arguments.exists("key2"));
        assertTrue(arguments.exists("enabled"));
        assertFalse(arguments.exists("disabled"));

        val properties = arguments.getProperties();
        assertEquals("value1", properties.getProperty("key1"));
        assertNull(properties.getProperty("key2"));
        assertEquals("enabled", properties.getProperty("enabled"));
        assertNull(properties.getProperty("disabled"));

        assertEquals("value1", arguments.getStr("key1"));
        assertNull(arguments.getStr("key2"));
        assertEquals("enabled", arguments.getStr("enabled"));
        assertNull(arguments.getStr("disabled"));

        Arguments.initial(
                "--custom1.key1=value1",
                "--custom1.key2=value2",
                "--custom2.key1=value2",
                "--custom2.key2=value1");

        val empty = arguments.subset("");
        assertTrue(empty.getProperties().isEmpty());

        val custom1 = arguments.subset("custom1");
        assertEquals(arguments.getStr("custom1.key1"), custom1.getStr("key1"));
        assertEquals(arguments.getStr("custom1.key2"), custom1.getStr("key2"));

        val custom2 = arguments.subset("custom2.");
        assertEquals(arguments.getStr("custom2.key1"), custom2.getStr("key1"));
        assertEquals(arguments.getStr("custom2.key2"), custom2.getStr("key2"));
    }
}
