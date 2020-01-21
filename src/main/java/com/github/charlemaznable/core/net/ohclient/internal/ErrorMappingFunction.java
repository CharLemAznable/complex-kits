package com.github.charlemaznable.core.net.ohclient.internal;

import java.util.function.Function;

import static org.joor.Reflect.onClass;

public final class ErrorMappingFunction
        implements Function<Class<? extends RuntimeException>, Object> {

    @Override
    public Object apply(Class<? extends RuntimeException> exClass) {
        throw (RuntimeException) onClass(exClass).create().get();
    }
}
