package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClzPathTest {

    @Test
    public void testClassExists() {
        assertFalse(ClzPath.classExists("com.github.charlemaznable.lang.ClzPathNon"));
        assertTrue(ClzPath.classExists("com.github.charlemaznable.lang.ClzPath"));
    }

    @Test
    public void testFindClass() {
        assertNull(ClzPath.findClass(""));
        assertNull(ClzPath.findClass("com.github.charlemaznable.lang.ClzPathNon"));
        assertNotNull(ClzPath.findClass("com.github.charlemaznable.lang.ClzPath"));
    }

    @Test
    public void testTryLoadClass() {
        assertNull(ClzPath.tryLoadClass(""));
        assertNull(ClzPath.tryLoadClass("com.github.charlemaznable.lang.ClzPathNon"));
        assertNotNull(ClzPath.tryLoadClass("com.github.charlemaznable.lang.ClzPath"));
    }

    @Test
    public void testClassResources() {
        assertNotEquals(0, ClzPath.classResources("com/github/charlemaznable/lang", "class").length);
    }
}
