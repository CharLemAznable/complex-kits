package com.github.charlemaznable.lang;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.time.Duration;

import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.LoadingCachee.get;
import static com.github.charlemaznable.lang.LoadingCachee.getAll;
import static com.github.charlemaznable.lang.LoadingCachee.getUnchecked;
import static com.github.charlemaznable.lang.Str.toStr;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadingCacheeTest {

    @Test
    public void testSimpleCache() {
        LoadingCache<String, String> simpleCache =
                LoadingCachee.simpleCache(new CacheLoader<String, String>() {
                    @Override
                    public String load(@Nonnull String s) {
                        if (s.equals("ex")) {
                            throw new RuntimeException("ex");
                        }
                        return s + toStr(currentTimeMillis());
                    }
                });

        assertEquals(get(simpleCache, "abc"), get(simpleCache, "abc"));
        assertEquals(get(simpleCache, "abc"), getAll(simpleCache, newArrayList("abc")).get("abc"));

        assertThrows(RuntimeException.class, () -> get(simpleCache, "ex"));
        assertThrows(RuntimeException.class, () -> getUnchecked(simpleCache, "ex"));
        assertThrows(RuntimeException.class, () -> getAll(simpleCache, newArrayList("ex")));
    }

    @SneakyThrows
    @Test
    public void testAccessCache() {
        LoadingCache<String, String> accessCache =
                LoadingCachee.accessCache(new CacheLoader<String, String>() {
                    @Override
                    public String load(@Nonnull String s) {
                        return s + toStr(currentTimeMillis());
                    }
                }, Duration.ofMillis(20));

        String cachedValue = get(accessCache, "abc");
        assertEquals(cachedValue, get(accessCache, "abc"));

        Thread.sleep(10);
        assertEquals(cachedValue, get(accessCache, "abc"));

        Thread.sleep(20);
        assertNotEquals(cachedValue, get(accessCache, "abc"));
    }

    @SneakyThrows
    @Test
    public void testWriteCache() {
        LoadingCache<String, String> writeCache =
                LoadingCachee.writeCache(new CacheLoader<String, String>() {
                    @Override
                    public String load(@Nonnull String s) {
                        return s + toStr(currentTimeMillis());
                    }
                }, Duration.ofMillis(20));

        String cachedValue = get(writeCache, "abc");
        assertEquals(cachedValue, get(writeCache, "abc"));

        Thread.sleep(10);
        assertEquals(cachedValue, get(writeCache, "abc"));

        Thread.sleep(20);
        assertNotEquals(cachedValue, get(writeCache, "abc"));
    }
}
