package com.github.charlemaznable.core.net.common;

import java.util.function.BiFunction;

public interface FallbackFunction<R, T>
        extends BiFunction<Integer, R, T> {

    @Override
    T apply(Integer statusCode, R responseBody);
}
