package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmptyTest {

    @Test
    public void testEmptyCollection() {
        assertTrue(Empty.isEmpty(null));
        assertFalse(Empty.isEmpty(new EmptyObject()));
        var list = newArrayList();
        assertTrue(Empty.isEmpty(list));
        list.add("test");
        assertFalse(Empty.isEmpty(list));
    }

    static class EmptyObject {}
}
