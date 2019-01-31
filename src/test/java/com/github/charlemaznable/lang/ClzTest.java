package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClzTest {

    @Test
    public void testAssignable() {
        assertFalse(Clz.isAssignable(Integer.class, String.class));
        assertTrue(Clz.isAssignable(Integer.class, Number.class));
    }

    @Test
    public void testConcrete() {
        assertFalse(Clz.isConcrete(Serializable.class));
        assertFalse(Clz.isConcrete(Number.class));
        assertTrue(Clz.isConcrete(Integer.class));
    }

    @Test
    public void testGetMethod() {
        assertThrows(NoSuchMethodException.class, () -> Clz.getMethod(Integer.class, "nonExistsMethod"));
        assertDoesNotThrow(() -> Clz.getMethod(Integer.class, "toString"));
    }
}
