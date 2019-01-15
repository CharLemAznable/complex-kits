package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.charlemaznable.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IterableeTest {

    private List<String> list = newArrayList("aaa", "bbb", "ccc");

    @Test
    public void testForEach() {
        new Iterablee();
        Iterablee.forEach(list, (index, string) -> {
            if (index == 0) assertEquals("aaa", string);
            if (index == 1) assertEquals("bbb", string);
            if (index == 2) assertEquals("ccc", string);
        });
    }

    @Test
    public void testFind() {
        assertEquals(0, Iterablee.find(list, "aaa"::equals));
        assertEquals(Iterablee.NOT_FOUND, Iterablee.find(list, "ddd"::equals));
    }

    @Test
    public void testFindWithIndex() {
        assertEquals(0, Iterablee.find(list, (index, string) -> "aaa".equals(string)));
        assertEquals(Iterablee.NOT_FOUND, Iterablee.find(list, (index, string) -> "ddd".equals(string)));
    }
}
