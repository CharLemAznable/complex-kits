package com.github.charlemaznable.codec;


import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.codec.Bytes.bytes;
import static com.github.charlemaznable.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseXTest {

    @Test
    public void testBaseX() {
        BaseX base64 = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
        assertEquals("ASDFGHJ", string(base64.decode(base64.encode(bytes("ASDFGHJ")))));
        BaseX base92 = new BaseX("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz`1234567890-=~!@#$%^&*()_+[]{}|;':,./<>?");
        assertEquals("ASDFGHJ", string(base92.decode(base92.encode(bytes("ASDFGHJ")))));
        assertEquals("ASDFGHJ", string(Base92.unBase92(Base92.base92(bytes("ASDFGHJ")))));
    }
}
