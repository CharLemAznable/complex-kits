package com.github.charlemaznable.lang;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.github.charlemaznable.lang.Listt.listFromMap;
import static com.github.charlemaznable.lang.Mapp.mapFromList;
import static com.github.charlemaznable.lang.Mapp.of;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class MapListTest {

    @Test
    public void testMapList() {
        List<Map<String, String>> list = newArrayList();
        list.add(of("key", "key1", "value", "value1"));
        list.add(of("key", "key2", "value", "value2"));
        Map<String, String> map = mapFromList(list, "key", "value");
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));

        List<Map<String, String>> list2 = listFromMap(map, "key", "value");
        assertIterableEquals(list, list2);
    }
}
