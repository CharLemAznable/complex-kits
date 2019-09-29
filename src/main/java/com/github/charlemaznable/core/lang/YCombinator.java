package com.github.charlemaznable.core.lang;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

public class YCombinator {

    public static <T, R> Function<T, R> of(Function<Function<T, R>, Function<T, R>> f) {
        return n -> f.apply(of(f)).apply(n);
    }

    public static <T, R> Function<T, R> of(CacheableFunction<T, R> f) {
        return n -> nullThen(f.getIfPresent(n), () -> f.put(n, f.apply(of(f)).apply(n)));
    }

    public static abstract class CacheableFunction<T, R>
            implements Function<Function<T, R>, Function<T, R>> {

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
