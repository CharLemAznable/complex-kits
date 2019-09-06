package com.github.charlemaznable.core.codec;

public class Base92 {

    private static BaseX base92 = new BaseX(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`1234567890-=~!@#$%^&*()_+[]{}|;':,./<>?");

    public static String base92(byte[] bytes) {
        return base92.encode(bytes);
    }

    public static byte[] unBase92(String value) {
        return base92.decode(value);
    }
}
