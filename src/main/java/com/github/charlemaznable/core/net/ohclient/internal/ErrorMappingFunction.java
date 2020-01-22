package com.github.charlemaznable.core.net.ohclient.internal;

import lombok.AllArgsConstructor;
import org.joor.ReflectException;

import java.util.function.Function;

import static org.joor.Reflect.onClass;

@AllArgsConstructor
public final class ErrorMappingFunction
        implements Function<Class<? extends RuntimeException>, Object> {

    private String responseContent;

    @Override
    public Object apply(Class<? extends RuntimeException> exClass) {
        try {
            throw (RuntimeException) onClass(exClass).create(responseContent).get();
        } catch (ReflectException e) {
            throw (RuntimeException) onClass(exClass).create().get();
        }
    }
}
