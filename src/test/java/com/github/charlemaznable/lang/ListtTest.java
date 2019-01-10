package com.github.charlemaznable.lang;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


public class ListtTest {

    @Test
    public void testShuffle() {
        List<Integer> list = new ArrayList<>();
        List<Integer> newList = Listt.shuffle(list);
        assertEquals(newList, list);

        for (int i = 0; i < 10; i++) list.add(i);
        newList = Listt.shuffle(list);
        assertNotEquals(newList, list);
    }
}
