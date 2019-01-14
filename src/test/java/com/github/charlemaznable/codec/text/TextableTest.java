package com.github.charlemaznable.codec.text;

import com.github.charlemaznable.codec.text.Textable.Processor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextableTest {

    @Data
    public static class TestTextable extends Textable {

        private int key1;
        private String key2;
    }

    @Test
    public void testTextable() {
        TestTextable bean = new TestTextable();
        bean.setKey1(42);
        bean.setKey2("answer");
        assertEquals("key1=42&key2=answer", bean.toText());
        assertEquals("key1='42'&key2='answer'", bean.toText(new Processor() {
            @Override
            public String process(String origin) {
                return "'" + origin + "'";
            }
        }));
    }
}
