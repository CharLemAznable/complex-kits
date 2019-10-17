package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.charlemaznable.core.lang.Iterablee.NOT_FOUND;
import static com.github.charlemaznable.core.lang.Iterablee.find;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IterableeTest {

    private List<String> list = newArrayList("aaa", "bbb", "ccc");

    @Test
    public void testForEach() {
        Iterablee.forEach(list, (index, string) -> {
            if (index == 0) assertEquals("aaa", string);
            if (index == 1) assertEquals("bbb", string);
            if (index == 2) assertEquals("ccc", string);
        });
    }

    @Test
    public void testFind() {
        assertEquals(0, find(list, "aaa"::equals));
        assertEquals(NOT_FOUND, find(list, "ddd"::equals));
    }

    @Test
    public void testFindWithIndex() {
        assertEquals(0, find(list, (index, string) -> "aaa".equals(string)));
        assertEquals(NOT_FOUND, find(list, (index, string) -> "ddd".equals(string)));
    }
}
