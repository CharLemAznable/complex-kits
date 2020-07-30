package com.github.charlemaznable.core.lang;

import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Listt.isEmpty;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Listt.newArrayListOfType;
import static com.github.charlemaznable.core.lang.Listt.shuffle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListtTest {

    @Test
    public void testUnique() {
        var temp = newArrayList("aaa", "aaa", "aaa");
        var list = newArrayList(temp.iterator());
        var unique = Listt.unique(list);
        assertEquals(1, unique.size());
        assertEquals("aaa", unique.get(0));
    }

    @Test
    public void testShuffle() {
        var list = newArrayList();
        var newList = shuffle(list);
        assertEquals(newList, list);

        for (var i = 0; i < 10; i++) list.add(i);
        newList = shuffle(list);
        assertNotEquals(newList, list);
    }

    @Test
    public void testIsEmpty() {
        var list = newArrayListOfType(1, String.class);
        assertFalse(isEmpty(list));
        list.clear();
        assertTrue(isEmpty(list));
    }
}
