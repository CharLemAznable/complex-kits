package com.github.charlemaznable.codec;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Bytes.string;

public class Base16 {

    public static String base16(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            if ((aByte & 0xFF) < 16) sb.append("0");
            sb.append(Long.toString(aByte & 0xFF, 16));
        }
        return sb.toString();
    }

    public static String base16(String str) {
        return base16(bytes(str));
    }

    public static byte[] unBase16(String value) {
        byte[] bytes = new byte[value.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(
                    value.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static String unBase16AsString(String value) {
        return string(unBase16(value));
    }
}
