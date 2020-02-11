package com.github.charlemaznable.core.net.common;

import java.lang.reflect.Method;

public interface FixedValueProvider {

    default String value(Class<?> clazz, String name) {
        throw new ProviderException(this.getClass().getName()
                + "#value(Class<?>, String) need be overwritten");
    }

    default String value(Class<?> clazz, Method method, String name) {
        throw new ProviderException(this.getClass().getName()
                + "#value(Class<?>, Method, String) need be overwritten");
    }
}
