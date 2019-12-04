package com.github.charlemaznable.core.lang;

import com.google.common.cache.CacheLoader;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.time.Duration;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.LoadingCachee.accessCache;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.getAll;
import static com.github.charlemaznable.core.lang.LoadingCachee.getUnchecked;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.LoadingCachee.writeCache;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static java.lang.System.currentTimeMillis;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LoadingCacheeTest {

    @Test
    public void testSimpleCache() {
        val simpleCache = simpleCache(new CacheLoader<String, String>() {
            @Override
            public String load(@NonNull @Nonnull String s) {
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
            public String load(@NonNull @Nonnull String s) {
                return s + toStr(currentTimeMillis());
            }
        }, Duration.ofMillis(200));

        val cachedValue = get(accessCache, "abc");
        assertEquals(cachedValue, get(accessCache, "abc"));

        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(100)).until(() ->
                        cachedValue.equals(get(accessCache, "abc"))));

        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(300)).until(() ->
                        !cachedValue.equals(get(accessCache, "abc"))));
    }

    @SneakyThrows
    @Test
    public void testWriteCache() {
        val writeCache = writeCache(new CacheLoader<String, String>() {
            @Override
            public String load(@NonNull @Nonnull String s) {
                return s + toStr(currentTimeMillis());
            }
        }, Duration.ofMillis(100));

        val cachedValue = get(writeCache, "abc");
        assertEquals(cachedValue, get(writeCache, "abc"));

        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(50)).until(() ->
                        cachedValue.equals(get(writeCache, "abc"))));

        assertDoesNotThrow(() ->
                await().pollDelay(Duration.ofMillis(150)).until(() ->
                        !cachedValue.equals(get(writeCache, "abc"))));
    }
}
