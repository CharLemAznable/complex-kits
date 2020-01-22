package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.ohclient.exception.OhError;
import lombok.AllArgsConstructor;
import lombok.val;
import okhttp3.ResponseBody;

import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static org.joor.Reflect.onClass;

@AllArgsConstructor
public final class ErrorMappingFunction
        implements Function<Class<? extends OhError>, Object> {

    private int statusCode;
    private ResponseBody responseBody;

    @Override
    public Object apply(Class<? extends OhError> exClass) {
        val message = toStr(notNullThen(responseBody,
                ResponseBodyExtractor::string));
        throw (OhError) onClass(exClass)
                .create(statusCode, message).get();
    }
}
