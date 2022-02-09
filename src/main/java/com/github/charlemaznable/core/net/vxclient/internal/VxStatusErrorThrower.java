package com.github.charlemaznable.core.net.vxclient.internal;

import com.github.charlemaznable.core.net.common.StatusError;

public final class VxStatusErrorThrower
        implements VxFallbackFunction<Void> {

    @Override
    public Void apply(Integer statusCode, String responseBody) {
        throw new StatusError(statusCode, responseBody);
    }
}
