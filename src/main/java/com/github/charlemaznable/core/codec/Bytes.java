package com.github.charlemaznable.core.codec;

import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class Bytes {

    private Bytes() {}

    /**
     * default UTF-8
     */
    public static byte[] bytes(String str) {
        return bytes(str, UTF_8);
    }

    /**
     * default UTF-8
     */
    public static String string(byte[] bytes) {
        return string(bytes, UTF_8);
    }

    public static byte[] bytes(String str, Charset charset) {
        return str == null ? null : str.getBytes(charset);
    }

    public static String string(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }
}
