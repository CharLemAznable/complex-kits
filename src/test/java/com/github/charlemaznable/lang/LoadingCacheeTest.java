package com.github.charlemaznable.lang;

import com.google.common.cache.CacheLoader;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.time.Duration;

import static com.github.charlemaznable.lang.Listt.newArrayList;
import static com.github.charlemaznable.lang.LoadingCachee.accessCache;
import static com.github.charlemaznable.lang.LoadingCachee.get;
import static com.github.charlemaznable.lang.LoadingCachee.getAll;
import static com.github.charlemaznable.lang.LoadingCachee.getUnchecked;
import static com.github.charlemaznable.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.lang.LoadingCachee.writeCache;
import static com.github.charlemaznable.lang.Str.toStr;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadingCacheeTest {

    @Test
    public void testSimpleCache() {
        val simpleCache = simpleCache(new CacheLoader<String, String>() {
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
        val accessCache = accessCache(new CacheLoader<String, String>() {
            @Override
            public String load(@Nonnull String s) {
                return s + toStr(currentTimeMillis());
            }
        }, Duration.ofMillis(100));

        val cachedValue = get(accessCache, "abc");
        assertEquals(cachedValue, get(accessCache, "abc"));

        Thread.sleep(50);
        assertEquals(cachedValue, get(accessCache, "abc"));

        Thread.sleep(150);
        assertNotEquals(cachedValue, get(accessCache, "abc"));
    }

    @SneakyThrows
    @Test
    public void testWriteCache() {
        val writeCache = writeCache(new CacheLoader<String, String>() {
            @Override
            public String load(@Nonnull String s) {
                return s + toStr(currentTimeMillis());
            }
        }, Duration.ofMillis(100));

        val cachedValue = get(writeCache, "abc");
        assertEquals(cachedValue, get(writeCache, "abc"));

        Thread.sleep(50);
        assertEquals(cachedValue, get(writeCache, "abc"));

        Thread.sleep(150);
        assertNotEquals(cachedValue, get(writeCache, "abc"));
    }
}
