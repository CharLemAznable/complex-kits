package com.github.charlemaznable.core.spring;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public class AnnotationElf {

    private AnnotationElf() {
        throw new UnsupportedOperationException();
    }

    public static Class<? extends Annotation> resolveContainerAnnotationType(Class<? extends Annotation> annotationType) {
        Repeatable repeatable = getAnnotation(annotationType, Repeatable.class);
        return repeatable != null ? repeatable.value() : null;
    }
}
