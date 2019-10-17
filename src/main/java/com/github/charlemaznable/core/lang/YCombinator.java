package com.github.charlemaznable.core.lang;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

public class YCombinator {

    private YCombinator() {}

    public static <T, R> Function<T, R> of(UnaryFunction<Function<T, R>> f) {
        return n -> f.apply(of(f)).apply(n);
    }

    public static <T, R> Function<T, R> of(CacheableUnaryFunction<T, R> f) {
        return n -> nullThen(f.getIfPresent(n), () -> f.put(n, f.apply(of(f)).apply(n)));
    }

    @FunctionalInterface
    public interface UnaryFunction<T> extends UnaryOperator<T> {}

    public abstract static class CacheableUnaryFunction<T, R>
            implements UnaryFunction<Function<T, R>> {

        private Cache<T, R> cache = CacheBuilder.newBuilder().build();

        public R getIfPresent(T key) {
            return cache.getIfPresent(key);
        }

        public R put(T key, R value) {
            cache.put(key, value);
            return value;
        }
    }
}
