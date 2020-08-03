package com.github.charlemaznable.core.net.ohclient.internal;

import lombok.SneakyThrows;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OhResponseBody extends ResponseBody {

    private MediaType contentType;
    private long contentLength;
    private Buffer buffer;

    @SneakyThrows
    public OhResponseBody(ResponseBody responseBody) {
        this.contentType = responseBody.contentType();
        this.contentLength = responseBody.contentLength();

        val source = responseBody.source();
        source.request(Long.MAX_VALUE);
        this.buffer = source.getBuffer().clone();
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return this.contentType;
    }

    @Override
    public long contentLength() {
        return this.contentLength;
    }

    @Nonnull
    @Override
    public BufferedSource source() {
        return this.buffer.clone();
    }
}
