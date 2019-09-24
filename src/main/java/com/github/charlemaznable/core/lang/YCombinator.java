package com.github.charlemaznable.core.lang;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.val;

import java.util.function.Function;

public class YCombinator {

    public static <T, R> Function<T, R> of(Function<Function<T, R>, Function<T, R>> b) {
        Function<Function<Function<T, R>, Function<T, R>>, Function<T, R>> g = f -> {
            Function<Function, Function<T, R>> r = p -> {
                @SuppressWarnings("unchecked")
                val w = (Function<Function, Function<T, R>>) p;
                return f.apply(param -> w.apply(w).apply(param));
            };
            return r.apply(r);
        };
        return g.apply(b);
    }

    public static <T, R> Function<T, R> of(CacheableFunction<T, R> c) {
        Function<Function<Function<T, R>, Function<T, R>>, Function<T, R>> g = f -> {
            Function<Function, Function<T, R>> r = p -> {
                @SuppressWarnings("unchecked")
                val w = (Function<Function, Function<T, R>>) p;
                return f.apply(param -> {
                    R present = c.getIfPresent(param);
                    if (null != present) return present;
                    R result = w.apply(w).apply(param);
                    return c.put(param, result);
                });
            };
            return r.apply(r);
        };
        return g.apply(c);
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
