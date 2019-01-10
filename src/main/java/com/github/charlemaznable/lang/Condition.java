package com.github.charlemaznable.lang;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.charlemaznable.lang.Empty.isEmpty;
import static com.github.charlemaznable.lang.Str.isBlank;
import static com.github.charlemaznable.lang.Str.isNotBlank;

public class Condition {

    @SafeVarargs
    public static <T> T nonNull(T... objects) {
        for (T object : objects) {
            if (null != object) return object;
        }
        return null;
    }

    @SafeVarargs
    public static <T> T nonEmpty(T... objects) {
        for (T object : objects) {
            if (!isEmpty(object)) return object;
        }
        return null;
    }

    public static String nonBlank(String... strings) {
        for (String string : strings) {
            if (isNotBlank(string)) return string;
        }
        return null;
    }

    public static <T, R> R notNullThen(T object, Function<? super T, R> action) {
        return null == object ? null : action.apply(object);
    }

    public static <T, R> R notEmptyThen(T object, Function<? super T, R> action) {
        return isEmpty(object) ? null : action.apply(object);
    }

    public static <R> R notBlankThen(String string, Function<? super String, R> action) {
        return isBlank(string) ? null : action.apply(string);
    }

    public static <T> T nullThen(T object, Supplier<T> action) {
        return null == object ? action.get() : object;
    }

    public static <T> T emptyThen(T object, Supplier<T> action) {
        return isEmpty(object) ? action.get() : object;
    }

    public static String blankThen(String string, Supplier<String> action) {
        return isBlank(string) ? action.get() : string;
    }
}
