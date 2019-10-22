package com.github.charlemaznable.core.codec;

import lombok.val;
import lombok.var;

import java.io.ByteArrayOutputStream;

public class Base62 {

    private static char[] encodes = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    private static byte[] decodes = new byte[256];

    static {
        for (var i = 0; i < encodes.length; i++) {
            decodes[encodes[i]] = (byte) i;
        }
    }

    private Base62() {}

    public static String base64(byte[] data) {
        val sb = new StringBuilder(data.length * 2);
        var pos = 0;
        var val = 0;
        for (val b : data) {
            val = (val << 8) | (b & 0xFF);
            pos += 8;
            while (pos > 5) {
                pos -= 6;
                sb.append(encodes[val >> pos]);
                val &= ((1 << pos) - 1);
            }
        }
        if (pos > 0) {
            sb.append(encodes[val << (6 - pos)]);
        }
        return sb.toString();
    }

    public static byte[] unBase64(String dataStr) {
        val data = dataStr.toCharArray();
        val baos = new ByteArrayOutputStream(data.length);
        var pos = 0;
        var val = 0;
        for (val c : data) {
            val = (val << 6) | (decodes[c] & 0xff);
            pos += 6;
            while (pos > 7) {
                pos -= 8;
                baos.write(val >> pos);
                val &= ((1 << pos) - 1);
            }
        }
        return baos.toByteArray();
    }

    public static String base62(byte[] data) {
        val sb = new StringBuilder(data.length * 2);
        var pos = 0;
        var val = 0;
        for (val b : data) {
            val = (val << 8) | (b & 0xFF);
            pos += 8;
            while (pos > 5) {
                pos -= 6;
                val c = encodes[val >> pos];
                sb.append(translate(c));
                val &= ((1 << pos) - 1);
            }
        }
        if (pos > 0) {
            val c = encodes[val << (6 - pos)];
            sb.append(translate(c));
        }
        return sb.toString();
    }

    private static String translate(char c) {
        if (c == 'i') return "ia";
        if (c == '+') return "ib";
        return c == '/' ? "ic" : String.valueOf(c);
    }

    public static byte[] unBase62(String dataStr) {
        val data = dataStr.toCharArray();
        val baos = new ByteArrayOutputStream(data.length);
        var pos = 0;
        var val = 0;
        var step = 1;
        for (var i = 0; i < data.length; i += step, step = 1) {
            var c = data[i];
            if (c == 'i') {
                c = data[i + 1];
                if (c == 'a') c = 'i';
                if (c == 'b') c = '+';
                if (c == 'c') c = '/';
                if (c == data[i + 1])
                    c = data[i];
                else step = 2;
            }
            val = (val << 6) | (decodes[c] & 0xff);
            pos += 6;
            while (pos > 7) {
                pos -= 8;
                baos.write(val >> pos);
                val &= ((1 << pos) - 1);
            }
        }
        return baos.toByteArray();
    }
}
