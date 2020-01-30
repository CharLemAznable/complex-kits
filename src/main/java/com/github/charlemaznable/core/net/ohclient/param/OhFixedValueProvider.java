package com.github.charlemaznable.core.net.ohclient.param;

import com.github.charlemaznable.core.net.ohclient.exception.OhException;

import java.lang.reflect.Method;

public interface OhFixedValueProvider {

    default String value(Class<?> clazz, String name) {
        throw new OhException(this.getClass().getName()
                + "#value(Class<?>, String) need be overwritten");
    }

    default String value(Class<?> clazz, Method method, String name) {
        throw new OhException(this.getClass().getName()
                + "#value(Class<?>, Method, String) need be overwritten");
    }
}
