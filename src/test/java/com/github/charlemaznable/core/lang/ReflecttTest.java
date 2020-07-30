package com.github.charlemaznable.core.lang;

import org.joor.ReflectException;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReflecttTest {

    @Test
    public void testReflectt() {
        assertThrows(ReflectException.class, () ->
                onClass(Reflectt.class).create().get());

        var pubNameField = Reflectt.field0(DemoReflect.class, "pubName");
        assertEquals("pubName", pubNameField.getName());
        var prvNameField = Reflectt.field0(DemoReflect.class, "prvName");
        assertEquals("prvName", prvNameField.getName());

        assertThrows(ReflectException.class, () ->
                Reflectt.field0(DemoReflect.class, "noneName"));
    }

    public static class DemoReflect {

        public String pubName;
        private String prvName;
    }
}
