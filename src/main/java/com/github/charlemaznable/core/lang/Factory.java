package com.github.charlemaznable.core.lang;

@FunctionalInterface
public interface Factory {

    <T> T build(Class<T> clazz);
}
