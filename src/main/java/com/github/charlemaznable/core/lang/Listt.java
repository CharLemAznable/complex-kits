package com.github.charlemaznable.core.lang;

import com.google.common.collect.Lists;
import lombok.val;
import lombok.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.github.charlemaznable.core.lang.Mapp.of;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.currentTimeMillis;
import static org.joor.Reflect.onClass;

public class Listt {

    private static Random shuffleRandom = new Random(currentTimeMillis());

    public static <T> List<T> unique(Iterable<T> original) {
        return newArrayList(newHashSet(original));
    }

    public static boolean isEmpty(Collection list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNotEmpty(Collection list) {
        return list != null && !list.isEmpty();
    }

    public static <T> List<T> shuffle(Iterable<T> original) {
        List<T> newList = newArrayList(original);
        Collections.shuffle(newList, shuffleRandom);
        return newList;
    }

    public static <T> List<Map<String, T>> listFromMap(Map<T, T> map, String keyKey, String valueKey) {
        if (map == null) return newArrayList();

        List<Map<String, T>> result = newArrayList();
        for (val entry : map.entrySet()) {
            result.add(of(keyKey, entry.getKey(), valueKey, entry.getValue()));
        }
        return result;
    }

    public static <E> ArrayList<E> newArrayList() {
        return Lists.newArrayList();
    }

    @SafeVarargs
    public static <E> ArrayList<E> newArrayList(E... elements) {
        return null == elements ? Lists.newArrayList() : Lists.newArrayList(elements);
    }

    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        return null == elements ? Lists.newArrayList() : Lists.newArrayList(elements);
    }

    public static <E> ArrayList<E> newArrayList(Iterator<? extends E> elements) {
        return null == elements ? Lists.newArrayList() : Lists.newArrayList(elements);
    }

    public static <E> ArrayList<E> newArrayListOfType(int count, Class<? extends E> itemType) {
        ArrayList<E> list = Lists.newArrayList();
        for (var i = 0; i < count; ++i) {
            list.add(onClass(itemType).create().get());
        }
        return list;
    }
}