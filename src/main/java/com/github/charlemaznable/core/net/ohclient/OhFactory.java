package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.net.ohclient.internal.OhDummy;
import com.github.charlemaznable.core.net.ohclient.internal.OhProxy;
import com.github.charlemaznable.core.spring.SpringContext;
import com.google.common.cache.LoadingCache;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.google.common.cache.CacheLoader.from;

public final class OhFactory {

    private static OhLoader springOhLoader = new OhLoader();

    private OhFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> T getClient(Class<T> ohClass) {
        return springOhLoader.getClient(ohClass);
    }

    public static OhLoader ohLoader(Factory providerFactory) {
        return new OhLoader(providerFactory);
    }

    @SuppressWarnings("unchecked")
    public static class OhLoader {

        private LoadingCache<Class, Object> ohCache
                = LoadingCachee.simpleCache(from(this::loadClient));
        private Factory providerFactory;

        private OhLoader() {
            this(null);
        }

        private OhLoader(Factory providerFactory) {
            this.providerFactory = nullThen(providerFactory,
                    () -> SpringContext::getBeanOrCreate);
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
                            new OhProxy(ohClass, providerFactory),
                            NoOp.INSTANCE}, null);
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new OhException(clazz + " is not An Interface");
        }
    }
}
