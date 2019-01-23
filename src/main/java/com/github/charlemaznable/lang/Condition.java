package com.github.charlemaznable.lang;

import com.github.charlemaznable.lang.ex.BlankStringException;
import com.github.charlemaznable.lang.ex.EmptyObjectException;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @CanIgnoreReturnValue
    public static <T> T checkNotNull(T object) {
        return Preconditions.checkNotNull(object);
    }

    @CanIgnoreReturnValue
    public static <T> T checkNotNull(T object, @Nullable Object errorMessage) {
        return Preconditions.checkNotNull(object, errorMessage);
    }

    @CanIgnoreReturnValue
    public static <T> T checkNotNull(T object, @NonNull RuntimeException errorException) {
        if (null == object) {
            throw nullThen(errorException, NullPointerException::new);
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static <T> T checkNotEmpty(T object) {
        if (isEmpty(object)) {
            throw new EmptyObjectException();
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static <T> T checkNotEmpty(T object, @Nullable Object errorMessage) {
        if (isEmpty(object)) {
            throw new EmptyObjectException(String.valueOf(errorMessage));
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static <T> T checkNotEmpty(T object, @NonNull RuntimeException errorException) {
        if (isEmpty(object)) {
            throw nullThen(errorException, EmptyObjectException::new);
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static String checkNotBlank(String string) {
        if (isBlank(string)) {
            throw new BlankStringException();
        }
        return string;
    }

    @CanIgnoreReturnValue
    public static String checkNotBlank(String string, @Nullable Object errorMessage) {
        if (isBlank(string)) {
            throw new BlankStringException(String.valueOf(errorMessage));
        }
        return string;
    }

    @CanIgnoreReturnValue
    public static String checkNotBlank(String string, @NonNull RuntimeException errorException) {
        if (isBlank(string)) {
            throw nullThen(errorException, BlankStringException::new);
        }
        return string;
    }
}
