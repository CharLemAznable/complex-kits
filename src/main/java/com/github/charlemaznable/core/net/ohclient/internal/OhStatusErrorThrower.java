package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.StatusError;
import lombok.val;
import okhttp3.ResponseBody;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.toStr;

public final class OhStatusErrorThrower
        implements OhFallbackFunction<Void> {

    @Override
    public Void apply(Integer statusCode, ResponseBody responseBody) {
        val message = toStr(notNullThen(responseBody,
                ResponseBodyExtractor::string));
        throw new StatusError(statusCode, message);
    }
}
