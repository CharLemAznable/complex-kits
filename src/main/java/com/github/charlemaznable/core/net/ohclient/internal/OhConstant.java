package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.google.common.net.HttpHeaders;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.net.common.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class OhConstant {

    public static final String ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET;
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE;
    public static final ContentFormatter DEFAULT_CONTENT_FORMATTER = new FormContentFormatter();
    public static final HttpMethod DEFAULT_HTTP_METHOD = GET;
    public static final long DEFAULT_CALL_TIMEOUT = 0;
    public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
    public static final long DEFAULT_READ_TIMEOUT = 10_000;
    public static final long DEFAULT_WRITE_TIMEOUT = 10_000;
    public static final Level DEFAULT_LOGGING_LEVEL = Level.NONE;

    private OhConstant() {
        throw new UnsupportedOperationException();
    }
}
