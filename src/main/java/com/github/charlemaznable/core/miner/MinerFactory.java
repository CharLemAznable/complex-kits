package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.ExpiringEntryLoaderr;
import com.github.charlemaznable.core.lang.ExpiringMapp;
import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.lang.Str;
import com.github.charlemaznable.core.miner.MinerConfig.DataIdProvider;
import com.github.charlemaznable.core.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.core.miner.MinerConfig.GroupProvider;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;
import lombok.NoArgsConstructor;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpiringValue;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.AbstractMiner;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.impl.DiamondUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.context.FactoryContext.SpringFactory.springFactory;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class MinerFactory {

    private static StringSubstitutor minerMinerSubstitutor;
    private static StringSubstitutor minerClassPathSubstitutor;
    private static LoadingCache<Factory, MinerLoader> minerLoaderCache
            = LoadingCachee.simpleCache(CacheLoader.from(MinerLoader::new));

    private MinerFactory() {
        throw new UnsupportedOperationException();
    }

    public static <T> T getMiner(Class<T> minerClass) {
        return minerLoader(FactoryContext.get()).getMiner(minerClass);
    }

    public static MinerLoader springMinerLoader() {
        return minerLoader(springFactory());
    }

    public static MinerLoader minerLoader(Factory factory) {
        return get(minerLoaderCache, factory);
    }

    private static String substitute(String source) {
        if (isNull(minerMinerSubstitutor)) {
            minerMinerSubstitutor = minerAsSubstitutor("Env", "miner");
        }
        if (isNull(minerClassPathSubstitutor)) {
            minerClassPathSubstitutor = classResourceAsSubstitutor("miner.env.props");
        }
        return minerClassPathSubstitutor.replace(minerMinerSubstitutor.replace(source));
    }

    @SuppressWarnings("unchecked")
    public static class MinerLoader {

        private Factory factory;
        private LoadingCache<Class, Object> minerCache
                = LoadingCachee.simpleCache(CacheLoader.from(this::loadMiner));
        private ExpiringMap<Class, Minerable> minerableCache
                = ExpiringMapp.expiringMap(ExpiringEntryLoaderr.from(this::loadMinerable));

        MinerLoader(Factory factory) {
            this.factory = checkNotNull(factory);
        }

        public <T> T getMiner(Class<T> minerClass) {
            return (T) get(minerCache, minerClass);
        }

        <T> Minerable getMinerable(Class<T> minerClass) {
            return minerableCache.get(minerClass);
        }

        private <T> Object loadMiner(Class<T> minerClass) {
            ensureClassIsAnInterface(minerClass);
            checkClassConfig(minerClass);

            val minerProxy = new MinerProxy(minerClass, factory, this);
            return EasyEnhancer.create(MinerDummy.class,
                    new Class[]{minerClass, Minerable.class},
                    method -> {
                        if (method.isDefault()) return 1;
                        return 0;
                    }, new Callback[]{minerProxy, NoOp.INSTANCE}, null);
        }

        private <T> void ensureClassIsAnInterface(Class<T> clazz) {
            if (clazz.isInterface()) return;
            throw new MinerConfigException(clazz + " is not An Interface");
        }

        private <T> MinerConfig checkClassConfig(Class<T> clazz) {
            return checkNotNull(getAnnotation(clazz, MinerConfig.class),
                    new MinerConfigException(clazz + " has no MinerConfig"));
        }

        private <T> ExpiringValue<Minerable> loadMinerable(Class<T> clazz) {
            val minerConfig = checkClassConfig(clazz);
            val group = checkMinerGroup(clazz, minerConfig);
            val minerable = new Miner(blankThen(group, () -> "DEFAULT_GROUP"));
            val dataId = checkMinerDataId(clazz, minerConfig);
            val value = isNotBlank(dataId) ? minerable.getMiner(dataId) : minerable;
            val cacheSeconds = Math.max(0, minerConfig.cacheSeconds());
            return new ExpiringValue(value, cacheSeconds, TimeUnit.SECONDS);
        }

        private <T> String checkMinerGroup(Class<T> clazz, MinerConfig minerConfig) {
            val providerClass = minerConfig.groupProvider();
            return substitute(GroupProvider.class == providerClass ? minerConfig.group()
                    : FactoryContext.apply(factory, providerClass, p -> p.group(clazz)));
        }

        private <T> String checkMinerDataId(Class<T> clazz, MinerConfig minerConfig) {
            val providerClass = minerConfig.dataIdProvider();
            return substitute(DataIdProvider.class == providerClass ? minerConfig.dataId()
                    : FactoryContext.apply(factory, providerClass, p -> p.dataId(clazz)));
        }
    }

    private static class MinerProxy<T> implements MethodInterceptor {

        private Class<T> minerClass;
        private Factory factory;
        private MinerLoader minerLoader;
        private ExpiringMap<Method, Pair<String, String>> stoneCache
                = ExpiringMapp.expiringMap(ExpiringEntryLoaderr.from(this::loadStone));

        public MinerProxy(Class<T> minerClass, Factory factory, MinerLoader minerLoader) {
            this.minerClass = minerClass;
            this.factory = factory;
            this.minerLoader = minerLoader;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            if (method.getDeclaringClass().equals(MinerDummy.class)) {
                return methodProxy.invokeSuper(o, args);
            }

            if (method.getDeclaringClass().equals(Minerable.class)) {
                return method.invoke(minerLoader.getMinerable(minerClass), args);
            }

            val cacheStone = stoneCache.get(method);
            val stone = cacheStone.getLeft();
            val defaultValue = cacheStone.getRight();
            val defaultArgument = args.length > 0 ? args[0] : null;

            if (nonNull(stone)) return convertType(stone, method);
            if (nonNull(defaultArgument)) return defaultArgument;
            if (nonNull(defaultValue)) return convertType(defaultValue, method);
            return null;
        }

        @SuppressWarnings("unchecked")
        private ExpiringValue<Pair<String, String>> loadStone(Method method) {
            val minerConfig = findAnnotation(method, MinerConfig.class);
            String group = checkMinerGroup(method, minerConfig);
            val dataId = checkMinerDataId(method, minerConfig);
            val defaultValue = checkMinerDefaultValue(method, minerConfig);
            val minerable = minerLoader.getMinerable(minerClass);
            // group blank:
            //   if minerable instanceof AbstractMiner
            //     use its defaultGroupName
            //   else do nothing
            String stoneGroup = blankThen(group, () -> minerable instanceof AbstractMiner ?
                    ((AbstractMiner) minerable).getDefaultGroupName() : group);
            String stone = minerable.getStone(stoneGroup, blankThen(dataId, method::getName));
            val cacheSeconds = checkMinerCacheSeconds(minerConfig);
            return new ExpiringValue(Pair.of(stone, defaultValue), cacheSeconds, TimeUnit.SECONDS);
        }

        private String checkMinerGroup(Method method, MinerConfig minerConfig) {
            if (isNull(minerConfig)) return "";
            val providerClass = minerConfig.groupProvider();
            return substitute(GroupProvider.class == providerClass ? minerConfig.group()
                    : FactoryContext.apply(factory, providerClass, p -> p.group(minerClass, method)));
        }

        private String checkMinerDataId(Method method, MinerConfig minerConfig) {
            if (isNull(minerConfig)) return "";
            val providerClass = minerConfig.dataIdProvider();
            return substitute(DataIdProvider.class == providerClass ? minerConfig.dataId()
                    : FactoryContext.apply(factory, providerClass, p -> p.dataId(minerClass, method)));
        }

        private String checkMinerDefaultValue(Method method, MinerConfig minerConfig) {
            if (isNull(minerConfig)) return null;
            val providerClass = minerConfig.defaultValueProvider();
            String defaultValue = minerConfig.defaultValue();
            if (DefaultValueProvider.class != providerClass) {
                defaultValue = FactoryContext.apply(factory, providerClass,
                        p -> p.defaultValue(minerClass, method));
            }
            return substitute(blankThen(defaultValue, () -> null));
        }

        private long checkMinerCacheSeconds(MinerConfig minerConfig) {
            if (isNull(minerConfig)) return 0;
            return Math.max(0, minerConfig.cacheSeconds());
        }

        private Object convertType(String value, Method method) {
            val rt = Primitives.unwrap(method.getReturnType());
            if (rt == String.class) return value;
            if (rt.isPrimitive()) return parsePrimitive(rt, value);

            if (Map.class.isAssignableFrom(rt))
                return parseProperties(value);

            val grt = method.getGenericReturnType();
            val isCollection = grt instanceof ParameterizedType
                    && Collection.class.isAssignableFrom(rt);
            if (!isCollection) return parseObject(rt, value);

            return parseObjects((Class) ((ParameterizedType) grt)
                    .getActualTypeArguments()[0], value);
        }

        @SuppressWarnings("Duplicates")
        private Object parsePrimitive(Class<?> rt, String value) {
            if (rt == boolean.class) return Str.anyOfIgnoreCase(value, "yes", "true", "on", "y");
            if (rt == short.class) return Short.parseShort(value);
            if (rt == int.class) return Integer.parseInt(value);
            if (rt == long.class) return Long.parseLong(value);
            if (rt == float.class) return Float.parseFloat(value);
            if (rt == double.class) return Double.parseDouble(value);
            if (rt == byte.class) return Byte.parseByte(value);
            if (rt == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
            return null;
        }

        private Properties parseProperties(String value) {
            return DiamondUtils.parseStoneToProperties(value);
        }

        private Object parseObject(Class<?> rt, String value) {
            return DiamondUtils.parseObject(value, rt);
        }

        private Object parseObjects(Class<?> rt, String value) {
            return DiamondUtils.parseObjects(value, rt);
        }
    }

    @NoArgsConstructor
    private static class MinerDummy {

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MinerDummy && hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            return "Miner@" + Integer.toHexString(hashCode());
        }
    }
}
