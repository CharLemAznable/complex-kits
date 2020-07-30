package com.github.charlemaznable.core.codec;

import java.math.BigInteger;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public final class Hex {

    private Hex() {}

    public static String hex(byte[] array) {
        var bi = new BigInteger(1, array);
        var hex = bi.toString(16);
        var paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            var f = "%0" + paddingLength + "d";
            return format(f, 0) + hex;
        } else {
            return hex;
        }
    }

    public static byte[] unHex(String hex) {
        var bytes = new byte[hex.length() / 2];
        for (var i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
