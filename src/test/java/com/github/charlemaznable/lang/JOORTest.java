package com.github.charlemaznable.lang;


import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JOORTest {

    private static String privateMethod() {
        return "PrivateMethod";
    }

    @Test
    public void testJOORInvokePrivateMethod() {
        assertEquals("PrivateMethod", on(JOORTest.class).call("privateMethod").get());
    }
}
