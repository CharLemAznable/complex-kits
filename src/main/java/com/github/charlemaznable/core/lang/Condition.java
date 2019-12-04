package com.github.charlemaznable.core.lang;

import com.github.charlemaznable.core.lang.ex.BadConditionException;
import com.github.charlemaznable.core.lang.ex.BlankStringException;
import com.github.charlemaznable.core.lang.ex.EmptyObjectException;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.val;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Empty.isEmpty;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;

public class Condition {

    private Condition() {}

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

    public static short nonEquals(short base, short... numbers) {
        for (val number : numbers) {
            if (base != number) return number;
        }
        return base;
    }

    public static int nonEquals(int base, int... numbers) {
        for (val number : numbers) {
            if (base != number) return number;
        }
        return base;
    }

    public static long nonEquals(long base, long... numbers) {
        for (val number : numbers) {
            if (base != number) return number;
        }
        return base;
    }

    public static float nonEquals(float base, float... numbers) {
        for (val number : numbers) {
            if (base != number) return number;
        }
        return base;
    }

    public static double nonEquals(double base, double... numbers) {
        for (val number : numbers) {
            if (base != number) return number;
        }
        return base;
    }

    public static byte nonEquals(byte base, byte... bytes) {
        for (val b : bytes) {
            if (base != b) return b;
        }
        return base;
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
    public static @NonNull <T> T checkNotNull(T object) {
        return Preconditions.checkNotNull(object);
    }

    @CanIgnoreReturnValue
    public static @NonNull <T> T checkNotNull(T object, @Nullable Object errorMessage) {
        return Preconditions.checkNotNull(object, errorMessage);
    }

    @CanIgnoreReturnValue
    public static @NonNull <T> T checkNotNull(T object, @Nullable RuntimeException errorException) {
        if (null == object) {
            throw nullThen(errorException, NullPointerException::new);
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static @NonNull <T> T checkNotEmpty(T object) {
        if (isEmpty(object)) {
            throw new EmptyObjectException();
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static @NonNull <T> T checkNotEmpty(T object, @Nullable Object errorMessage) {
        if (isEmpty(object)) {
            throw new EmptyObjectException(String.valueOf(errorMessage));
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static @NonNull <T> T checkNotEmpty(T object, @Nullable RuntimeException errorException) {
        if (isEmpty(object)) {
            throw nullThen(errorException, EmptyObjectException::new);
        }
        return object;
    }

    @CanIgnoreReturnValue
    public static @NonNull String checkNotBlank(String string) {
        if (isBlank(string)) {
            throw new BlankStringException();
        }
        return string;
    }

    @CanIgnoreReturnValue
    public static @NonNull String checkNotBlank(String string, @Nullable Object errorMessage) {
        if (isBlank(string)) {
            throw new BlankStringException(String.valueOf(errorMessage));
        }
        return string;
    }

    @CanIgnoreReturnValue
    public static @NonNull String checkNotBlank(String string, @Nullable RuntimeException errorException) {
        if (isBlank(string)) {
            throw nullThen(errorException, BlankStringException::new);
        }
        return string;
    }

    public static void checkCondition(BooleanSupplier condition) {
        if (!condition.getAsBoolean()) throw new BadConditionException();
    }

    public static void checkCondition(BooleanSupplier condition, @Nullable Object errorMessage) {
        if (!condition.getAsBoolean()) throw new BadConditionException(String.valueOf(errorMessage));
    }

    public static void checkCondition(BooleanSupplier condition, @Nullable RuntimeException errorException) {
        if (!condition.getAsBoolean()) throw nullThen(errorException, BadConditionException::new);
    }

    public static void checkCondition(BooleanSupplier condition, Executable executable) {
        if (!condition.getAsBoolean()) throw new BadConditionException();
        executable.execute();
    }

    public static void checkCondition(BooleanSupplier condition, Executable executable, @Nullable Object errorMessage) {
        if (!condition.getAsBoolean()) throw new BadConditionException(String.valueOf(errorMessage));
        executable.execute();
    }

    public static void checkCondition(BooleanSupplier condition, Executable executable, @Nullable RuntimeException errorException) {
        if (!condition.getAsBoolean()) throw nullThen(errorException, BadConditionException::new);
        executable.execute();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(BooleanSupplier condition, Supplier<T> action) {
        if (!condition.getAsBoolean()) throw new BadConditionException();
        return action.get();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(BooleanSupplier condition, Supplier<T> action, @Nullable Object errorMessage) {
        if (!condition.getAsBoolean()) throw new BadConditionException(String.valueOf(errorMessage));
        return action.get();
    }

    @CanIgnoreReturnValue
    public static <T> T checkCondition(BooleanSupplier condition, Supplier<T> action, @Nullable RuntimeException errorException) {
        if (!condition.getAsBoolean()) throw nullThen(errorException, BadConditionException::new);
        return action.get();
    }
}
