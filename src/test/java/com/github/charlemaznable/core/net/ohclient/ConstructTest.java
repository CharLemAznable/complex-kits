package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.internal.OhConstant;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConstructTest {

    @Test
    public void testOhConstant() {
        assertThrows(ReflectException.class, () ->
                onClass(OhConstant.class).create().get());
    }
}
