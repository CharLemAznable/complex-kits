package com.github.charlemaznable.codec.text;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextableTest {

    @Test
    public void testTextable() {
        TestTextable bean = new TestTextable();
        bean.setKey1(42);
        bean.setKey2("answer");
        assertEquals("key1=42&key2=answer", bean.toText());
        assertEquals("key1='42'&key2='answer'", bean.toText(origin -> "'" + origin + "'"));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TestTextable extends Textable {

        private int key1;
        private String key2;
    }
}
