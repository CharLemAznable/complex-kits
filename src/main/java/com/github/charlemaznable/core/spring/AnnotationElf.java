package com.github.charlemaznable.core.spring;

import lombok.val;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class AnnotationElf {

    private AnnotationElf() {
        throw new UnsupportedOperationException();
    }

    public static Class<? extends Annotation> resolveContainerAnnotationType(Class<? extends Annotation> annotationType) {
        val repeatable = getAnnotation(annotationType, Repeatable.class);
        return repeatable != null ? repeatable.value() : null;
    }
}
