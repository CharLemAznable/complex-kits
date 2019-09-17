package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testClass.TestClass;
import com.github.charlemaznable.core.spring.testClass.TestSpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static com.github.charlemaznable.core.spring.ClzResolver.getAnnotatedClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getSubClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClzResolverTest {

    @Test
    public void testClzResolver() {
        new ClzResolver();

        val basePackage = "com.github.charlemaznable.core.spring.testClass";

        val classes = getClasses(basePackage);
        int countTestClass = 0;
        for (Class<?> clazz : classes) {
            if (TestClass.class == clazz)
                countTestClass++;
        }
        assertEquals(1, countTestClass);

        val subClasses = getSubClasses(basePackage, SpringContext.class);
        assertEquals(1, subClasses.size());
        val contextClass = subClasses.get(0);
        assertEquals(TestSpringContext.class, contextClass);

        val componentClasses = getAnnotatedClasses(basePackage, Component.class);
        assertEquals(1, componentClasses.size());
        val componentClass = componentClasses.get(0);
        assertEquals(TestSpringContext.class, componentClass);
    }
}
