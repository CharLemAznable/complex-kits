package com.github.charlemaznable.core.net.vxclient.internal;

import com.github.charlemaznable.core.net.common.StatusError;
import lombok.AllArgsConstructor;

import java.util.function.Function;

import static org.joor.Reflect.onClass;

@AllArgsConstructor
public class StatusErrorFunction
        implements Function<Class<? extends StatusError>, Void> {

    private int statusCode;
    private String responseBody;

    @Override
    public Void apply(Class<? extends StatusError> exClass) {
        throw (StatusError) onClass(exClass)
                .create(statusCode, responseBody).get();
    }
}
