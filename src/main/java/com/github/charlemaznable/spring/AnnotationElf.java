package com.github.charlemaznable.spring;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;

import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public class AnnotationElf {

    public static Class<? extends Annotation> resolveContainerAnnotationType(Class<? extends Annotation> annotationType) {
        Repeatable repeatable = getAnnotation(annotationType, Repeatable.class);
        return repeatable != null ? repeatable.value() : null;
    }
}
