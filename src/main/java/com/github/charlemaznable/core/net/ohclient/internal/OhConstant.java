package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.ContentFormat.ContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;

import java.nio.charset.Charset;

import static com.github.charlemaznable.core.net.common.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class OhConstant {

    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = "Content-Type";
    public static final ContentFormatter DEFAULT_CONTENT_FORMATTER = new FormContentFormatter();
    public static final HttpMethod DEFAULT_HTTP_METHOD = GET;

    private OhConstant() {
        throw new UnsupportedOperationException();
    }
}
