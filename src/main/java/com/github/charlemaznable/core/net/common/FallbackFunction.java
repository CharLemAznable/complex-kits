package com.github.charlemaznable.core.net.common;

import com.github.charlemaznable.core.net.common.FallbackFunction.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

public interface FallbackFunction<R>
        extends Function<Response, R> {

    @Override
    R apply(Response response);

    @AllArgsConstructor
    @Getter
    abstract class Response<T> {

        private int statusCode;
        private T responseBody;

        public abstract String responseBodyAsString();
    }
}
