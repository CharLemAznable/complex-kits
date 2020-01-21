package com.github.charlemaznable.core.net.ohclient.param;

import com.github.charlemaznable.core.net.ohclient.exception.OhException;

import java.lang.reflect.Method;

public interface OhFixedValueProvider {

    default String value(Class<?> clazz) {
        throw new OhException(this.getClass().getName()
                + "#value(Class<?>) need be overwritten");
    }

    default String value(Class<?> clazz, Method method) {
        throw new OhException(this.getClass().getName()
                + "#value(Class<?>, Method) need be overwritten");
    }
}
