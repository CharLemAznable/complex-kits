package com.github.charlemaznable.core.codec;

public final class Base92 {

    private static BaseX baseX92 = new BaseX(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`1234567890-=~!@#$%^&*()_+[]{}|;':,./<>?");

    private Base92() {}

    public static String base92(byte[] bytes) {
        return baseX92.encode(bytes);
    }

    public static byte[] unBase92(String value) {
        return baseX92.decode(value);
    }
}
