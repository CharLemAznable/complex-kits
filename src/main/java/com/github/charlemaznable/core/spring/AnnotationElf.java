package com.github.charlemaznable.core.spring;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class AnnotationElf {

    private AnnotationElf() {
        throw new UnsupportedOperationException();
    }

    public static Class<? extends Annotation> resolveContainerAnnotationType(Class<? extends Annotation> annotationType) {
        var repeatable = getAnnotation(annotationType, Repeatable.class);
        return nonNull(repeatable) ? repeatable.value() : null;
    }
}
