package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClzPathTest {

    @Test
    public void testClassExists() {
        new ClzPath();

        assertFalse(ClzPath.classExists("com.github.charlemaznable.core.lang.ClzPathNon"));
        assertTrue(ClzPath.classExists("com.github.charlemaznable.core.lang.ClzPath"));
    }

    @Test
    public void testFindClass() {
        assertNull(ClzPath.findClass(""));
        assertNull(ClzPath.findClass("com.github.charlemaznable.core.lang.ClzPathNon"));
        assertNotNull(ClzPath.findClass("com.github.charlemaznable.core.lang.ClzPath"));
    }

    @Test
    public void testTryLoadClass() {
        assertNull(ClzPath.tryLoadClass(""));
        assertNull(ClzPath.tryLoadClass("com.github.charlemaznable.core.lang.ClzPathNon"));
        assertNotNull(ClzPath.tryLoadClass("com.github.charlemaznable.core.lang.ClzPath"));
    }

    @Test
    public void testClassResources() {
        assertNotEquals(0, ClzPath.classResources("com/github/charlemaznable/core/lang", "class").length);
    }
}
