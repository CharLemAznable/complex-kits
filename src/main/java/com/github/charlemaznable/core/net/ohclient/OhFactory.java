package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.ohclient.internal.OhDummy;
import com.github.charlemaznable.core.net.ohclient.internal.OhProxy;
import com.google.common.cache.LoadingCache;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;

import static com.github.charlemaznable.core.context.FactoryContext.SpringFactory.springFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.google.common.cache.CacheLoader.from;

public final class OhFactory {

    private static LoadingCache<Factory, OhLoader> ohLoaderCache
            = LoadingCachee.simpleCache(from(OhLoader::new));

    private OhFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> T getClient(Class<T> ohClass) {
        return ohLoader(FactoryContext.get()).getClient(ohClass);
    }

    public static OhLoader springOhLoader() {
        return ohLoader(springFactory());
    }

    public static OhLoader ohLoader(Factory factory) {
        return LoadingCachee.get(ohLoaderCache, factory);
    }

    @SuppressWarnings("unchecked")
    public static class OhLoader {

        private Factory factory;
        private LoadingCache<Class, Object> ohCache
                = LoadingCachee.simpleCache(from(this::loadClient));

        OhLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        public <T> T getClient(Class<T> ohClass) {
            return (T) LoadingCachee.get(ohCache, ohClass);
        }

        private <T> Object loadClient(Class<T> ohClass) {
            ensureClassIsAnInterface(ohClass);
            return EasyEnhancer.create(OhDummy.class,
                    new Class[]{ohClass},
                    method -> {
                        if (method.isDefault()) return 1;
                        return 0;
                    }, new Callback[]{
                            new OhProxy(ohClass, factory),
                            NoOp.INSTANCE}, null);
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new OhException(clazz + " is not An Interface");
        }
    }
}
