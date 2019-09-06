package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JOORTest {

    private static String privateMethod() {
        return "PrivateMethod";
    }

    @Test
    public void testJOORInvokePrivateMethod() {
        assertEquals("PrivateMethod", onClass(JOORTest.class).call("privateMethod").get());
    }
}
