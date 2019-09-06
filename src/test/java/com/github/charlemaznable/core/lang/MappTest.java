package com.github.charlemaznable.core.lang;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MappTest {

    @Test
    public void testOf() {
        val map1 = Mapp.of("a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F");
        map1.put("g", null);
        val map2 = Mapp.of("a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g");
        assertEquals(map1, map2);

        val map3 = Mapp.map("a", "A", "b", "B", "c", "C", "d", "D", "e", "E", "f", "F", "g");
        assertEquals(map2, map3);

        assertFalse(Mapp.isEmpty(map3));
        map3.clear();
        assertTrue(Mapp.isEmpty(map3));

        val map4 = Mapp.newHashMap(map2);
        assertFalse(Mapp.isEmpty(map4));
        val map5 = Mapp.newHashMap(null);
        assertTrue(Mapp.isEmpty(map3));
    }

    @Test
    public void testGet() {
        assertNull(Mapp.getNum(null, "1"));

        val map = Mapp.map("1", null, "2", 2, "3", "3", "4", "four", "b", true, "o", "on");
        assertNull(Mapp.getNum(map, "1"));
        assertEquals(2, Mapp.getNum(map, "2"));
        assertEquals(3L, Mapp.getNum(map, "3"));
        assertThrows(ParseException.class, () -> Mapp.getNum(map, "4"));

        assertFalse(Mapp.getBool(null, "1"));
        assertFalse(Mapp.getBool(map, "1"));
        assertTrue(Mapp.getBool(map, "b"));
        assertTrue(Mapp.getBool(map, "2"));
        assertFalse(Mapp.getBool(map, "3"));
        assertFalse(Mapp.getBool(map, "4"));
        assertTrue(Mapp.getBool(map, "o"));

        assertNull(Mapp.getInt(map, "1"));
        assertEquals(Integer.valueOf(2), Mapp.getInt(map, "2"));

        assertNull(Mapp.getLong(map, "1"));
        assertEquals(Long.valueOf(3), Mapp.getLong(map, "3"));
    }
}
