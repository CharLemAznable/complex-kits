package com.github.charlemaznable.lang;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;

public class LoadingCachee {

    @SneakyThrows
    public static <K, V> V get
            (LoadingCache<K, V> cache, K key) {
        try {
            return cache.get(key);
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        }
    }

    @SneakyThrows
    public static <K, V> V getUnchecked
            (LoadingCache<K, V> cache, K key) {
        try {
            return cache.getUnchecked(key);
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        }
    }

    @SneakyThrows
    public static <K, V> ImmutableMap<K, V> getAll
            (LoadingCache<K, V> cache, Iterable<? extends K> keys) {
        try {
            return cache.getAll(keys);
        } catch (UncheckedExecutionException e) {
            throw e.getCause();
        }
    }
}
