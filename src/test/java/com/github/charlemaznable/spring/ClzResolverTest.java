package com.github.charlemaznable.spring;

import com.github.charlemaznable.spring.testClass.TestClass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.charlemaznable.spring.ClzResolver.getClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClzResolverTest {

    @Test
    public void testClzResolver() {
        List<Class<?>> classes = getClasses("com.github.charlemaznable.spring.testClass");
        assertEquals(3, classes.size());
        Class<?> testClass = classes.get(0);
        assertEquals(TestClass.class.getName(), testClass.getName());
    }
}
