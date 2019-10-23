package com.github.charlemaznable.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    public void testConfig() {
        assertEquals("value1", Config.getStr("key1"));
        assertEquals("value2", Config.getStr("key2"));
        assertEquals("value3", Config.getStr("key3"));
    }
}
