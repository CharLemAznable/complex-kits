package com.github.charlemaznable.codec;

import java.math.BigInteger;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

public class Hex {

    public static String hex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    public static byte[] unHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
