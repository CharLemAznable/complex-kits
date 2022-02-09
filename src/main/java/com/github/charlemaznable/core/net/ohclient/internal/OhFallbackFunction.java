package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.FallbackFunction;
import okhttp3.ResponseBody;

public interface OhFallbackFunction<T>
        extends FallbackFunction<ResponseBody, T> {

    @Override
    T apply(Integer statusCode, ResponseBody responseBody);
}
