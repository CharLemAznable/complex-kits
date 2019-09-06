package com.github.charlemaznable.core.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigTest {

    @Test
    public void testConfig() {
        new Config();

        assertEquals("value1", Config.getStr("key1"));
    }
}
