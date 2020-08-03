package com.github.charlemaznable.core.codec.text;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TextableTest {

    @Test
    public void testTextable() {
        val bean1 = new TestTextable1();
        bean1.setKey1(42);
        bean1.setKey2("answer");
        assertEquals("key1=42&key2=answer", bean1.toText());
        assertEquals("key1='42'&key2='answer'", bean1.toText(origin -> "'" + origin + "'"));

        val bean2 = new TestTextable2();
        bean2.setKey1(42);
        bean2.setKey2("answer");
        assertEquals("key1=42&key2=answer", bean2.toText());
        assertEquals("key1='42'&key2='answer'", bean2.toText(origin -> "'" + origin + "'"));
    }

    @Getter
    @Setter
    static class TestTextable1 extends Textable {

        private int key1;
        private String key2;
        private String key3;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    static class TestTextable2 extends Textable {

        private int key1;
        private String key2;
        private String key3;
        private String key4;

        @Override
        protected List<String> excludedKeys() {
            return newArrayList("key4");
        }
    }
}
