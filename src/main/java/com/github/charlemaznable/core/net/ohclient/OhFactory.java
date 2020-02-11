package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.ohclient.internal.OhDummy;
import com.github.charlemaznable.core.net.ohclient.internal.OhProxy;
import com.google.common.cache.LoadingCache;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;

import static com.google.common.cache.CacheLoader.from;

@SuppressWarnings("unchecked")
public final class OhFactory {

    private static LoadingCache<Class, Object> ohCache
            = LoadingCachee.simpleCache(from(OhFactory::loadClient));

    private OhFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> T getClient(Class<T> ohClass) {
        return (T) LoadingCachee.get(ohCache, ohClass);
    }

    private static <T> Object loadClient(Class<T> ohClass) {
        ensureClassIsAnInterface(ohClass);
        return EasyEnhancer.create(OhDummy.class,
                new Class[]{ohClass},
                method -> {
                    if (method.isDefault()) return 1;
                    return 0;
                }, new Callback[]{
                        new OhProxy(ohClass),
                        NoOp.INSTANCE}, null);
    }

    private static <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;
        throw new OhException(clazz + " is not An Interface");
    }
}
