package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testcontext.TestAnnotation;
import com.github.charlemaznable.core.spring.testcontext.TestClass;
import com.github.charlemaznable.core.spring.testcontext.TestSpringContext;
import com.github.charlemaznable.core.spring.testcontext.TestSubSpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.List;

import static com.github.charlemaznable.core.spring.ClzResolver.getAnnotatedClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getClasses;
import static com.github.charlemaznable.core.spring.ClzResolver.getResources;
import static com.github.charlemaznable.core.spring.ClzResolver.getSubClasses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClzResolverTest {

    @Test
    public void testClzResolver() {
        val basePackage = "com.github.charlemaznable.core.spring.testcontext";

        val classes = getClasses(basePackage);
        int countTestClass = 0;
        for (val clazz : classes) {
            if (TestClass.class == clazz)
                countTestClass++;
        }
        assertEquals(1, countTestClass);

        List<Class<?>> subClasses = getSubClasses(basePackage, SpringContext.class);
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

    @Test
    public void testGetResources() {
        assertNotEquals(0, getResources("com/github/charlemaznable/core/lang", "class").toArray(new URL[0]).length);
        assertTrue(getResources("", "class").toArray(new URL[0]).length > 0);
    }
}
