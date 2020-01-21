package com.github.charlemaznable.core.net.ohclient.internal;

import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.ContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.FormContentFormat;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

public final class OhConstant {

    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final Charset DEFAULT_ACCEPT_CHARSET = UTF_8;
    public static final String CONTENT_TYPE = "Content-Type";
    public static final ContentFormat DEFAULT_CONTENT_FORMAT = new FormContentFormat();
    public static final RequestMethod DEFAULT_REQUEST_METHOD = GET;

    private OhConstant() {
        throw new UnsupportedOperationException();
    }
}
