package com.github.charlemaznable.core.codec;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.codec.Base92.base92;
import static com.github.charlemaznable.core.codec.Base92.unBase92;
import static com.github.charlemaznable.core.codec.Bytes.bytes;
import static com.github.charlemaznable.core.codec.Bytes.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Base92Test {

    @Test
    public void testBase92() {
        assertEquals("ASDFGHJ", string(unBase92(base92(bytes("ASDFGHJ")))));
    }
}
