package com.github.charlemaznable.spring;

import com.github.charlemaznable.spring.testClass.TestClass;
import com.github.charlemaznable.spring.testClass.TestSpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static com.github.charlemaznable.spring.ClzResolver.getAnnotatedClasses;
import static com.github.charlemaznable.spring.ClzResolver.getClasses;
import static com.github.charlemaznable.spring.ClzResolver.getSubClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClzResolverTest {

    @Test
    public void testClzResolver() {
        new ClzResolver();

        val basePackage = "com.github.charlemaznable.spring.testClass";

        val classes = getClasses(basePackage);
        assertEquals(3, classes.size());
        val testClass = classes.get(0);
        assertEquals(TestClass.class, testClass);

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
