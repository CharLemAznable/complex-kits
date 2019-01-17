package com.github.charlemaznable.lang;


import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.Listt.shuffle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class ListtTest {

    @Test
    public void testShuffle() {
        List<Integer> list = newArrayList();
        List<Integer> newList = shuffle(list);
        assertEquals(newList, list);

        for (int i = 0; i < 10; i++) list.add(i);
        newList = shuffle(list);
        assertNotEquals(newList, list);
    }
}
