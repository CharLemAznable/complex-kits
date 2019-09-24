package com.github.charlemaznable.core.codec;

import lombok.val;
import lombok.var;

public class BaseX {

    private int base;

    private char leader;

    private char[] chars;

    private byte[] bytes;

    public BaseX(String baseString) {
        this.base = baseString.length();
        this.leader = baseString.charAt(0);
        this.chars = baseString.toCharArray();
        this.bytes = new byte[256];
        for (var i = 0; i < base; i++) {
            this.bytes[this.chars[i]] = (byte) (i + 1);
        }
    }

    public String encode(byte[] data) {
        if (data.length == 0) return "";

        val digits = new int[data.length * 2];
        var digitpos = 0;
        for (val b : data) {
            var v = b & 0xFF;
            for (var j = 0; j <= digitpos; j++) {
                v |= digits[j] << 8;
                digits[j] = v % this.base;
                v /= this.base;
            }
            while (v > 0) {
                digits[++digitpos] = v % this.base;
                v /= this.base;
            }
        }
        for (var k = 0; k < data.length - 1; k++) {
            if (data[k] != 0) break;
            digits[++digitpos] = 0;
        }
        val chs = new char[digitpos + 1];
        for (var i = digitpos; i >= 0; i--) {
            chs[digitpos - i] = this.chars[digits[i]];
        }
        return new String(chs);
    }

    public byte[] decode(String value) {
        if (value == null || value.trim().isEmpty()) return new byte[0];

        val digits = new int[value.length()];
        var digitpos = 0;
        for (var i = 0; i < value.length(); i++) {
            var b = (int) this.bytes[value.charAt(i)];
            if (b == 0) throw new RuntimeException("Illegal character!");
            b -= 1;
            for (var j = 0; j <= digitpos; j++) {
                b += digits[j] * this.base;
                digits[j] = b & 0xFF;
                b >>= 8;
            }
            while (b > 0) {
                digits[++digitpos] = b & 0xFF;
                b >>= 8;
            }
        }
        for (var k = 0; k < value.length() - 1; k++) {
            if (value.charAt(k) != this.leader) break;
            digits[++digitpos] = 0;
        }
        val bytes = new byte[digitpos + 1];
        for (var i = digitpos; i >= 0; i--) {
            bytes[digitpos - i] = (byte) digits[i];
        }
        return bytes;
    }
}