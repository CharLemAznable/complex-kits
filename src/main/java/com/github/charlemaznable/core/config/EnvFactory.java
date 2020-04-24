package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.EnvConfig.ConfigKeyProvider;
import com.github.charlemaznable.core.config.EnvConfig.DefaultValueProvider;
import com.github.charlemaznable.core.config.ex.EnvConfigException;
import com.github.charlemaznable.core.config.impl.BaseConfigable;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.text.StringSubstitutor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static com.github.charlemaznable.core.context.FactoryContext.SpringFactory.springFactory;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class EnvFactory {

    private static StringSubstitutor envClassPathSubstitutor;
    private static LoadingCache<Factory, EnvLoader> envLoaderCache
            = LoadingCachee.simpleCache(from(EnvLoader::new));

    static {
        envClassPathSubstitutor = classResourceAsSubstitutor("config.env.props");
    }

    private EnvFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> T getEnv(Class<T> envClass) {
        return envLoader(FactoryContext.get()).getEnv(envClass);
    }

    public static EnvLoader springEnvLoader() {
        return envLoader(springFactory());
    }

    public static EnvLoader envLoader(Factory factory) {
        return LoadingCachee.get(envLoaderCache, factory);
    }

    @SuppressWarnings("unchecked")
    public static class EnvLoader {

        private Factory factory;
        private LoadingCache<Class, Object> envCache
                = LoadingCachee.simpleCache(from(this::loadEnv));

        EnvLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        public <T> T getEnv(Class<T> envClass) {
            return (T) LoadingCachee.get(envCache, envClass);
        }

        private <T> Object loadEnv(Class<T> envClass) {
            ensureClassIsAnInterface(envClass);
            checkEnvConfig(envClass);

            val envProxy = new EnvProxy(envClass, factory);
            return EasyEnhancer.create(EnvDummy.class,
                    new Class[]{envClass, Configable.class},
                    method -> {
                        if (method.isDefault()) return 1;
                        return 0;
                    }, new Callback[]{envProxy, NoOp.INSTANCE}, null);
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new EnvConfigException(clazz + " is not An Interface");
        }

        private <T> void checkEnvConfig(Class<T> clazz) {
            checkNotNull(getAnnotation(clazz, EnvConfig.class),
                    new EnvConfigException(clazz + " has no EnvConfig"));
        }
    }

    @NoArgsConstructor
    private static class EnvDummy {

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EnvDummy && hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            return "Env@" + Integer.toHexString(hashCode());
        }
    }

    @AllArgsConstructor
    private static class EnvProxy implements MethodInterceptor {

        private Class envClass;
        private Factory factory;

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            if (method.getDeclaringClass().equals(EnvDummy.class)) {
                return methodProxy.invokeSuper(o, args);
            }
            if (method.getDeclaringClass().equals(Configable.class)) {
                return method.invoke(Config.getConfigImpl(), args);
            }

            val envConfig = findAnnotation(method, EnvConfig.class);
            val configKey = checkEnvConfigKey(method, envConfig);
            var defaultValue = checkEnvDefaultValue(method, envConfig);
            var defaultArgument = args.length > 0 ? args[0] : null;

            val key = blankThen(configKey, method::getName);
            val value = Config.getStr(key);
            try {
                if (nonNull(value)) return parseValue(key, value, method);
            } catch (Exception e) {
                if (nonNull(defaultArgument)) return defaultArgument;
                if (nonNull(defaultValue)) return parseValue(key, defaultValue, method);
                throw e;
            }
            if (nonNull(defaultArgument)) return defaultArgument;
            if (nonNull(defaultValue)) return parseValue(key, defaultValue, method);
            return null;
        }

        private String checkEnvConfigKey(Method method, EnvConfig envConfig) {
            if (isNull(envConfig)) return "";
            val providerClass = envConfig.configKeyProvider();
            return substitute(ConfigKeyProvider.class == providerClass ? envConfig.configKey()
                    : FactoryContext.apply(factory, providerClass, p -> p.configKey(envClass, method)));
        }

        private String checkEnvDefaultValue(Method method, EnvConfig envConfig) {
            if (isNull(envConfig)) return null;
            val providerClass = envConfig.defaultValueProvider();
            String defaultValue = DefaultValueProvider.class == providerClass ? envConfig.defaultValue()
                    : FactoryContext.apply(factory, providerClass, p -> p.defaultValue(envClass, method));
            return substitute(blankThen(defaultValue, () -> null));
        }

        private String substitute(String source) {
            return envClassPathSubstitutor.replace(source);
        }

        private Object parseValue(String key, String value, Method method) {
            var rt = Primitives.unwrap(method.getReturnType());
            if (rt == String.class) return value;
            if (rt.isPrimitive()) return parsePrimitive(rt, key, value);

            val grt = method.getGenericReturnType();
            val isCollection = grt instanceof ParameterizedType
                    && Collection.class.isAssignableFrom(rt);
            if (!isCollection) return parseObject(rt, key, value);

            return parseObjects((Class) ((ParameterizedType) grt)
                    .getActualTypeArguments()[0], key, value);
        }

        public Object parsePrimitive(Class<?> rt, String key, String value) {
            BaseConfigable baseConfigable = (BaseConfigable) Config.getConfigImpl();
            if (rt == boolean.class) return baseConfigable.parseBool(key, value);
            if (rt == short.class) return (short) baseConfigable.parseInt(key, value);
            if (rt == int.class) return baseConfigable.parseInt(key, value);
            if (rt == long.class) return baseConfigable.parseLong(key, value);
            if (rt == float.class) return baseConfigable.parseFloat(key, value);
            if (rt == double.class) return baseConfigable.parseDouble(key, value);
            if (rt == byte.class) return Byte.parseByte(value);
            if (rt == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
            return null;
        }

        private Object parseObject(Class<?> rt, String key, String value) {
            return ((BaseConfigable) Config.getConfigImpl()).parseBean(key, value, rt);
        }

        private Object parseObjects(Class<?> rt, String key, String value) {
            return ((BaseConfigable) Config.getConfigImpl()).parseBeans(key, value, rt);
        }
    }
}
