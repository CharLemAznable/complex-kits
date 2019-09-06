package com.github.charlemaznable.core.spring;

import org.joor.ReflectException;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.stereotype.Component;

import static com.github.charlemaznable.core.spring.AnnotationElf.resolveContainerAnnotationType;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnnotationElfTest {

    @Test
    public void testAnnotationElf() {
        assertThrows(ReflectException.class, () -> onClass(AnnotationElf.class).create().get());

        assertNull(resolveContainerAnnotationType(Component.class));
        assertEquals(ComponentScans.class, resolveContainerAnnotationType(ComponentScan.class));
    }
}
