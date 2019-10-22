package com.github.charlemaznable.core.codec;

import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base92.base92;
import static com.github.charlemaznable.core.codec.Base92.unBase92;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseXTest {

    @Test
    public void testBaseX() {
        val base64 = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        assertEquals("ASDFGHJ", string(base64.decode(base64.encode(bytes("ASDFGHJ")))));
        assertEquals("", base64.encode(bytes("")));
        assertArrayEquals(new byte[0], base64.decode(null));
        assertArrayEquals(new byte[0], base64.decode(" "));
        val base92 = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`1234567890-=~!@#$%^&*()_+[]{}|;':,./<>?");
        assertEquals("ASDFGHJ", string(base92.decode(base92.encode(bytes("ASDFGHJ")))));
        assertEquals("ASDFGHJ", string(unBase92(base92(bytes("ASDFGHJ")))));
        assertEquals("", base92.encode(bytes("")));
        assertArrayEquals(new byte[0], base92.decode(null));
        assertArrayEquals(new byte[0], base92.decode(" "));
    }
}
