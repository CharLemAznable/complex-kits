package com.github.charlemaznable.core.spring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActiveProfilesThreadLocalTest {

    @Test
    public void testActiveProfilesThreadLocal() {
        ActiveProfilesThreadLocal.set(new String[]{"Test"});
        assertEquals(1, ActiveProfilesThreadLocal.get().length);
        assertEquals("Test", ActiveProfilesThreadLocal.get()[0]);
        ActiveProfilesThreadLocal.unload();
    }
}
