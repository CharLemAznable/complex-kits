package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeeTest {

    @Test
    public void testTypee() {
        assertEquals(String.class, Typee.getActualTypeArgument(TestActual.class, TestInterface.class));
    }

    interface TestInterface<T> {

        String getName(T instance);
    }

    static class TestActual implements TestInterface<String> {

        @Override
        public String getName(String instance) {
            return "\"" + instance + "\'";
        }
    }
}
