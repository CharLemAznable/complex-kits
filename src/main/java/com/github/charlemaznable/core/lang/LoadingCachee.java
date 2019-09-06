package com.github.charlemaznable.core.lang;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class LoadingCachee {

    public static <K, V> LoadingCache<K, V> simpleCache(CacheLoader<K, V> loader) {
        return newBuilder().build(loader);
    }

    public static <K, V> LoadingCache<K, V> accessCache(CacheLoader<K, V> loader, Duration duration) {
        return accessCache(loader, duration.toNanos(), NANOSECONDS);
    }

    public static <K, V> LoadingCache<K, V> accessCache(CacheLoader<K, V> loader, long duration, TimeUnit unit) {
        return newBuilder().expireAfterAccess(duration, unit).build(loader);
    }

    public static <K, V> LoadingCache<K, V> writeCache(CacheLoader<K, V> loader, Duration duration) {
        return writeCache(loader, duration.toNanos(), NANOSECONDS);
    }

    public static <K, V> LoadingCache<K, V> writeCache(CacheLoader<K, V> loader, long duration, TimeUnit unit) {
        return newBuilder().expireAfterWrite(duration, unit).build(loader);
    }

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
