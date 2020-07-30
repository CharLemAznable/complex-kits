package com.github.charlemaznable.core.codec;

import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static java.lang.Integer.parseInt;

public final class Base16 {

    private Base16() {}

    public static String base16(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (var aByte : bytes) {
            if ((aByte & 0xFF) < 16) sb.append("0");
            sb.append(Long.toString(aByte & 0xFF, 16));
        }
        return sb.toString();
    }

    public static String base16FromString(String str) {
        return base16(bytes(str));
    }

    public static byte[] unBase16(String value) {
        var bytes = new byte[value.length() / 2];
        for (var i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) parseInt(value.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    public static String unBase16AsString(String value) {
        return string(unBase16(value));
    }
}
