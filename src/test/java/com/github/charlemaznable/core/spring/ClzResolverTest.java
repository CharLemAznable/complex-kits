package com.github.charlemaznable.core.spring;

import com.github.charlemaznable.core.spring.testcontext.TestAnnotation;
import com.github.charlemaznable.core.spring.testcontext.TestClass;
import com.github.charlemaznable.core.spring.testcontext.TestSpringContext;
import com.github.charlemaznable.core.spring.testcontext.TestSubSpringContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.stream.Collectors;

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

        val subClasses1 = getSubClasses(basePackage, SpringContext.class);
        assertEquals(2, subClasses1.size());

        val subClasses2 = getSubClasses(basePackage, TestSpringContext.class);
        assertEquals(1, subClasses2.size());
        val contextClass = subClasses2.get(0);
        assertEquals(TestSubSpringContext.class, contextClass);

        val subClasses3 = getSubClasses(basePackage, TestSpringContext.class, true).stream()
                .sorted((o1, o2) -> StringUtils.compare(o1.getSimpleName(), o2.getSimpleName()))
                .collect(Collectors.toList());
        assertEquals(2, subClasses3.size());
        val contextClass31 = subClasses3.get(0);
        assertEquals(TestSpringContext.class, contextClass31);
        val contextClass32 = subClasses3.get(1);
        assertEquals(TestSubSpringContext.class, contextClass32);

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
