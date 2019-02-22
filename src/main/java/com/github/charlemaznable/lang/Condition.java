package com.github.charlemaznable.lang;

import com.github.charlemaznable.lang.ex.BlankStringException;
import com.github.charlemaznable.lang.ex.EmptyObjectException;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
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
        for (val object : objects) {
            if (null != object) return object;
        }
        return null;
    }

    @SafeVarargs
    public static <T> T nonEmpty(T... objects) {
        for (val object : objects) {
            if (!isEmpty(object)) return object;
        }
        return null;
    }

    public static String nonBlank(String... strings) {
        for (val string : strings) {
            if (isNotBlank(string)) return string;
        }
        return null;
    }

    public static <T, R> R notNullThen(T object, Function<? super T, R> action) {
        return checkNull(object, () -> null, action);
    }

    public static <T, R> R notEmptyThen(T object, Function<? super T, R> action) {
        return checkEmpty(object, () -> null, action);
    }

    public static <R> R notBlankThen(String string, Function<? super String, R> action) {
        return checkBlank(string, () -> null, action);
    }

    public static <T> T nullThen(T object, Supplier<T> action) {
        return checkNull(object, action, t -> t);
    }

    public static <T> T emptyThen(T object, Supplier<T> action) {
        return checkEmpty(object, action, t -> t);
    }

    public static String blankThen(String string, Supplier<String> action) {
        return checkBlank(string, action, s -> s);
    }

    public static <T, R> R checkNull(T object, Supplier<R> nullAction, Function<? super T, R> notNullAction) {
        return null == object ? nullAction.get() : notNullAction.apply(object);
    }

    public static <T, R> R checkEmpty(T object, Supplier<R> emptyAction, Function<? super T, R> notEmptyAction) {
        return isEmpty(object) ? emptyAction.get() : notEmptyAction.apply(object);
    }

    public static <R> R checkBlank(String string, Supplier<R> blankAction, Function<? super String, R> notBlankAction) {
        return isBlank(string) ? blankAction.get() : notBlankAction.apply(string);
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

    public static void checkCondition(Supplier<Boolean> condition) {
        if (!condition.get()) throw new RuntimeException();
    }

    public static void checkCondition(Supplier<Boolean> condition, @NonNull Object errorMessage) {
        if (!condition.get()) throw new RuntimeException(String.valueOf(errorMessage));
    }

    public static void checkCondition(Supplier<Boolean> condition, @NonNull RuntimeException errorException) {
        if (!condition.get()) throw nullThen(errorException, RuntimeException::new);
    }

    public static void checkCondition(Supplier<Boolean> condition, Executable executable) {
        if (!condition.get()) throw new RuntimeException();
        executable.execute();
    }

    public static void checkCondition(Supplier<Boolean> condition, Executable executable, @NonNull Object errorMessage) {
        if (!condition.get()) throw new RuntimeException(String.valueOf(errorMessage));
        executable.execute();
    }

    public static void checkCondition(Supplier<Boolean> condition, Executable executable, @NonNull RuntimeException errorException) {
        if (!condition.get()) throw nullThen(errorException, RuntimeException::new);
        executable.execute();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(Supplier<Boolean> condition, Supplier<T> action) {
        if (!condition.get()) throw new RuntimeException();
        return action.get();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(Supplier<Boolean> condition, Supplier<T> action, @NonNull Object errorMessage) {
        if (!condition.get()) throw new RuntimeException(String.valueOf(errorMessage));
        return action.get();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(Supplier<Boolean> condition, Supplier<T> action, @NonNull RuntimeException errorException) {
        if (!condition.get()) throw nullThen(errorException, RuntimeException::new);
        return action.get();
    }
}
