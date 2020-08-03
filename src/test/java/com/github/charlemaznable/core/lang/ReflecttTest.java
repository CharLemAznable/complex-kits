package com.github.charlemaznable.core.lang;

import lombok.val;
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

        val pubNameField = Reflectt.field0(DemoReflect.class, "pubName");
        assertEquals("pubName", pubNameField.getName());
        val prvNameField = Reflectt.field0(DemoReflect.class, "prvName");
        assertEquals("prvName", prvNameField.getName());

        assertThrows(ReflectException.class, () ->
                Reflectt.field0(DemoReflect.class, "noneName"));
    }

    public static class DemoReflect {

        public String pubName;
        private String prvName;
    }
}
