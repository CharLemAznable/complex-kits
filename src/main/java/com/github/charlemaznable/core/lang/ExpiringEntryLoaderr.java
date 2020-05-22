package com.github.charlemaznable.core.lang;

import net.jodah.expiringmap.ExpiringEntryLoader;
import net.jodah.expiringmap.ExpiringValue;

import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;

@SuppressWarnings("unchecked")
public final class ExpiringEntryLoaderr {

    private ExpiringEntryLoaderr() {
        throw new UnsupportedOperationException();
    }

    public static <K, V> ExpiringEntryLoader<K, V> from(Function<K, ExpiringValue<V>> function) {
        return new FunctionToExpiringEntryLoader(function);
    }

    private static final class FunctionToExpiringEntryLoader<K, V> implements ExpiringEntryLoader<K, V> {

        private final Function<K, ExpiringValue<V>> computingFunction;

        public FunctionToExpiringEntryLoader(Function<K, ExpiringValue<V>> computingFunction) {
            this.computingFunction = checkNotNull(computingFunction);
        }

        public ExpiringValue<V> load(K key) {
            return this.computingFunction.apply(checkNotNull(key));
        }
    }
}
