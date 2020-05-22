package com.github.charlemaznable.core.lang;

import net.jodah.expiringmap.ExpiringEntryLoader;
import net.jodah.expiringmap.ExpiringMap;

public final class ExpiringMapp {

    private ExpiringMapp() {}

    public static <K, V> ExpiringMap<K, V> expiringMap(ExpiringEntryLoader<K, V> loader) {
        return ExpiringMap.builder().expiringEntryLoader(loader).build();
    }
}
