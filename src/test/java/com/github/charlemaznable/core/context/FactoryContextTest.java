package com.github.charlemaznable.core.context;

import lombok.val;
import org.joor.ReflectException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.context.FactoryContext.SpringFactory.springFactory;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FactoryContextTest {

    @Test
    public void testFactoryContext() {
        assertThrows(ReflectException.class,
                () -> onClass(FactoryContext.class).create().get());

        assertNotNull(springFactory());
        assertSame(springFactory(), FactoryContext.get());

        FactoryContext.set(reflectFactory());
        assertNotSame(springFactory(), FactoryContext.get());

        FactoryContext.unload();
        assertSame(springFactory(), FactoryContext.get());

        FactoryContext.accept(reflectFactory(),
                TestInterface.class, Assertions::assertNull);
        assertSame(springFactory(), FactoryContext.get());

        val desc = FactoryContext.apply(reflectFactory(),
                TestClass.class, TestClass::desc);
        assertEquals("TestClass", desc);
        assertSame(springFactory(), FactoryContext.get());
    }

    public interface TestInterface {}

    public static class TestClass {

        String desc() {
            return "TestClass";
        }
    }
}
