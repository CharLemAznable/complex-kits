package com.github.charlemaznable.core.lang;

import lombok.val;
import lombok.var;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class Iterablee {

    public static final int NOT_FOUND = -1;

    public static <E> void forEach(Iterable<? extends E> elements,
                                   BiConsumer<Integer, ? super E> action) {
        requireNonNull(elements);
        requireNonNull(action);

        var index = 0;
        for (val element : elements) {
            action.accept(index++, element);
        }
    }

    public static <E> int find(Iterable<? extends E> elements,
                               BiFunction<Integer, ? super E, Boolean> action) {
        requireNonNull(elements);
        requireNonNull(action);

        var index = 0;
        for (val element : elements) {
            if (action.apply(index, element)) return index;
            index++;
        }
        return NOT_FOUND;
    }

    public static <E> int find(Iterable<? extends E> elements,
                               Function<? super E, Boolean> action) {
        requireNonNull(elements);
        requireNonNull(action);

        var index = 0;
        for (val element : elements) {
            if (action.apply(element)) return index;
            index++;
        }
        return NOT_FOUND;
    }
}