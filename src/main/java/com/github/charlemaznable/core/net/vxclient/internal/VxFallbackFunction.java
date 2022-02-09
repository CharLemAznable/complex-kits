package com.github.charlemaznable.core.net.vxclient.internal;

import com.github.charlemaznable.core.net.common.FallbackFunction;

public interface VxFallbackFunction<T>
        extends FallbackFunction<String, T> {

    @Override
    T apply(Integer statusCode, String responseBody);
}
