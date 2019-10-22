package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testClass.TestAnnotation;
import com.github.charlemaznable.core.spring.testClass.TestClass;
import com.github.charlemaznable.core.spring.testClass.TestSpringContext;
import com.github.charlemaznable.core.spring.testClass.TestSubSpringContext;
import lombok.val;
import lombok.var;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.spring.ClzResolver.getAnnotatedClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getSubClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClzResolverTest {

    @Test
    public void testClzResolver() {
        val basePackage = "com.github.charlemaznable.core.spring.testClass";

        val classes = getClasses(basePackage);
        int countTestClass = 0;
        for (Class<?> clazz : classes) {
            if (TestClass.class == clazz)
                countTestClass++;
        }
        assertEquals(1, countTestClass);

        var subClasses = getSubClasses(basePackage, SpringContext.class);
        assertEquals(2, subClasses.size());
        subClasses = getSubClasses(basePackage, TestSpringContext.class);
        assertEquals(1, subClasses.size());
        val contextClass = subClasses.get(0);
        assertEquals(TestSubSpringContext.class, contextClass);

        val annotatedClasses = getAnnotatedClasses(basePackage, TestAnnotation.class);
        assertEquals(1, annotatedClasses.size());
        val annotatedClass = annotatedClasses.get(0);
        assertEquals(TestSpringContext.class, annotatedClass);
    }
}
