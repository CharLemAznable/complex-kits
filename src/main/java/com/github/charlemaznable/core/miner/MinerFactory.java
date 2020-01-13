package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.LoadingCachee;
import com.github.charlemaznable.core.lang.Str;
import com.google.common.cache.LoadingCache;
import com.google.common.primitives.Primitives;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import lombok.var;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.impl.DiamondUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static com.google.common.cache.CacheLoader.from;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@SuppressWarnings("unchecked")
public class MinerFactory {

    private static LoadingCache<Class, Object> minerCache
            = LoadingCachee.simpleCache(from(MinerFactory::loadMiner));
    private static StringSubstitutor minerSubstitutor;

    static {
        minerSubstitutor = classResourceAsSubstitutor("miner.env.props");
    }

    private MinerFactory() {}

    public static <T> T getMiner(final Class<T> minerClass) {
        return (T) LoadingCachee.get(minerCache, minerClass);
    }

    private static <T> Object loadMiner(Class<T> minerClass) {
        ensureClassIsAnInterface(minerClass);
        val minerConfig = checkMinerConfig(minerClass);

        val group = minerSubstitutor.replace(minerConfig.group());
        val minerable = new Miner(blankThen(group, () -> "DEFAULT_GROUP"));
        val dataId = minerSubstitutor.replace(minerConfig.dataId());
        val minerProxy = new MinerProxy(isNotBlank(dataId)
                ? minerable.getMiner(dataId) : minerable);

        return EasyEnhancer.create(MinerDummy.class,
                new Class[]{minerClass, Minerable.class},
                method -> {
                    if (method.isDefault()) return 1;
                    return 0;
                }, new Callback[]{minerProxy, NoOp.INSTANCE}, null);
    }

    private static <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;
        throw new MinerConfigException(clazz + " is not An Interface");
    }

    private static <T> MinerConfig checkMinerConfig(Class<T> clazz) {
        return checkNotNull(findAnnotation(clazz, MinerConfig.class),
                new MinerConfigException(clazz + " has no MinerConfig"));
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class MinerDummy {}

    @AllArgsConstructor
    private static class MinerProxy implements MethodInterceptor {

        private Minerable minerable;

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            if (method.getDeclaringClass().equals(Minerable.class)) {
                return method.invoke(minerable, args);
            }

            val minerConfig = findAnnotation(method, MinerConfig.class);
            val group = minerSubstitutor.replace(
                    null != minerConfig ? minerConfig.group() : "");
            val dataId = minerSubstitutor.replace(
                    null != minerConfig ? minerConfig.dataId() : "");
            var defaultValue = minerSubstitutor.replace(
                    null != minerConfig ? blankThen(
                            minerConfig.defaultValue(), () -> null) : null);
            var defaultArgument = args.length > 0 ? args[0] : null;

            val stone = minerable.getStone(group, blankThen(dataId, method::getName));
            if (null != stone) return convertType(stone, method);
            if (null != defaultArgument) return defaultArgument;
            if (null != defaultValue) return convertType(defaultValue, method);
            return null;
        }

        private Object convertType(String value, Method method) {
            var rt = Primitives.unwrap(method.getReturnType());
            if (rt == String.class) return value;
            if (rt.isPrimitive()) return parsePrimitive(rt, value);

            val grt = method.getGenericReturnType();
            val isCollection = grt instanceof ParameterizedType
                    && Collection.class.isAssignableFrom(rt);
            if (!isCollection) return parseObject(rt, value);

            return parseObjects((Class) ((ParameterizedType) grt)
                    .getActualTypeArguments()[0], value);

        }

        private Object parsePrimitive(Class<?> rt, String value) {
            if (rt == boolean.class) return Str.anyOfIgnoreCase(value, "yes", "true", "on");
            if (rt == short.class) return Short.parseShort(value);
            if (rt == int.class) return Integer.parseInt(value);
            if (rt == long.class) return Long.parseLong(value);
            if (rt == float.class) return Float.parseFloat(value);
            if (rt == double.class) return Double.parseDouble(value);
            if (rt == byte.class) return Byte.parseByte(value);
            if (rt == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
            return null;
        }

        private Object parseObject(Class<?> rt, String value) {
            return DiamondUtils.parseObject(value, rt);
        }

        private Object parseObjects(Class<?> rt, String value) {
            return DiamondUtils.parseObjects(value, rt);
        }
    }
}
