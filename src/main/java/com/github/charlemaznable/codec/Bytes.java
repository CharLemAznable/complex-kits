package com.github.charlemaznable.codec;

import com.google.common.base.Charsets;

import java.nio.charset.Charset;

public class Bytes {

    /**
     * default UTF-8
     */
    public static byte[] bytes(String str) {
        return bytes(str, Charsets.UTF_8);
    }

    /**
     * default UTF-8
     */
    public static String string(byte[] bytes) {
        return string(bytes, Charsets.UTF_8);
    }

    public static byte[] bytes(String str, Charset charset) {
        return str == null ? null : str.getBytes(charset);
    }

    public static String string(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }
}
