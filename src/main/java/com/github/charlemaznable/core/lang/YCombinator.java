package com.github.charlemaznable.core.lang;

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
}
