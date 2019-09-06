package com.github.charlemaznable.core.lang;

import lombok.val;
import lombok.var;
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
        new Listt();

        val temp = newArrayList("aaa", "aaa", "aaa");
        val list = newArrayList(temp.iterator());
        val unique = Listt.unique(list);
        assertEquals(1, unique.size());
        assertEquals("aaa", unique.get(0));
    }

    @Test
    public void testShuffle() {
        val list = newArrayList();
        var newList = shuffle(list);
        assertEquals(newList, list);

        for (var i = 0; i < 10; i++) list.add(i);
        newList = shuffle(list);
        assertNotEquals(newList, list);
    }

    @Test
    public void testIsEmpty() {
        val list = newArrayListOfType(1, String.class);
        assertFalse(isEmpty(list));
        list.clear();
        assertTrue(isEmpty(list));
    }
}
