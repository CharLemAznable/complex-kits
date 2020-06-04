package com.github.charlemaznable.core.codec;

import java.math.BigInteger;

public final class Hex {

    private Hex() {}

    public static String hex(byte[] array) {
        return new BigInteger(1, array).toString(16);
    }

    public static byte[] unHex(String hex) {
        return new BigInteger(hex, 16).toByteArray();
    }

    public static String hex36(byte[] array) {
        return new BigInteger(1, array).toString(36);
    }

    public static byte[] unHex36(String hex36) {
        return new BigInteger(hex36, 36).toByteArray();
    }
}
