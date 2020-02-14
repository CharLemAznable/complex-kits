package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.StatusError;
import lombok.AllArgsConstructor;
import lombok.val;
import okhttp3.ResponseBody;

import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static org.joor.Reflect.onClass;

@AllArgsConstructor
public final class StatusErrorFunction
        implements Function<Class<? extends StatusError>, Void> {

    private int statusCode;
    private ResponseBody responseBody;

    @Override
    public Void apply(Class<? extends StatusError> exClass) {
        val message = toStr(notNullThen(responseBody,
                ResponseBodyExtractor::string));
        throw (StatusError) onClass(exClass)
                .create(statusCode, message).get();
    }
}
