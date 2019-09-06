package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.lang.EasyEnhancer;
import com.github.charlemaznable.core.lang.Str;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.primitives.Primitives;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;
import org.n3r.diamond.client.impl.DiamondUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.Str.isNotBlank;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@SuppressWarnings("unchecked")
@UtilityClass
public class MinerFactory {

    private Cache<Class, Object> minerCache = CacheBuilder.newBuilder().build();

    @SneakyThrows
    public <T> T getMiner(final Class<T> minerClass) {
        ensureClassIsAnInterface(minerClass);
        val minerConfig = checkMinerConfig(minerClass);
        return (T) minerCache.get(minerClass, () -> loadMiner(minerClass, minerConfig));
    }

    private <T> void ensureClassIsAnInterface(Class<T> clazz) {
        if (clazz.isInterface()) return;

        throw new MinerConfigException(clazz + " is not An Interface");
    }

    private <T> MinerConfig checkMinerConfig(Class<T> clazz) {
        return checkNotNull(findAnnotation(clazz, MinerConfig.class),
                new MinerConfigException(clazz + " has no MinerConfig"));
    }

    @SneakyThrows
    private Object loadMiner(Class minerClass, MinerConfig minerConfig) {
        val minerable = new Miner(blankThen(
                minerConfig.group(), () -> "DEFAULT_GROUP"));
        val dataId = minerConfig.dataId();
        val minerProxy = new MinerProxy(isNotBlank(dataId)
                ? minerable.getMiner(dataId) : minerable);

        return EasyEnhancer.create(MinerObject.class,
                new Class[]{minerClass, Minerable.class},
                method -> {
                    if (method.isDefault()) return 1;
                    return 0;
                }, new Callback[]{minerProxy, NoOp.INSTANCE}, null);
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class MinerObject {}

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
            val group = null != minerConfig ? minerConfig.group() : "";
            val dataId = null != minerConfig ? minerConfig.dataId() : "";
            var defaultValue = null != minerConfig ? minerConfig.defaultValue() : null;
            var defaultArgument = args.length > 0 ? args[0] : null;

            val stone = minerable.getStone(group, blankThen(dataId, method::getName));
            if (null != stone) return convertType(stone, method);
            if (null != defaultArgument) return defaultArgument;
            if (null != defaultValue) return convertType(defaultValue, method);
            return null;
        }

        private static Object convertType(String value, Method method) {
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

        private static Object parsePrimitive(Class<?> rt, String value) {
            if (rt == boolean.class) return Str.anyOfIgnoreCase(value, "yes", "true", "on");
            if (rt == short.class) return Short.parseShort(value);
            if (rt == int.class) return Integer.parseInt(value);
            if (rt == long.class) return Long.parseLong(value);
            if (rt == float.class) return Float.parseFloat(value);
            if (rt == double.class) return Double.parseDouble(value);
            if (rt == byte.class) return Byte.parseByte(value);
            return null;
        }

        private static Object parseObject(Class<?> rt, String value) {
            return DiamondUtils.parseObject(value, rt);
        }

        private static Object parseObjects(Class<?> rt, String value) {
            return DiamondUtils.parseObjects(value, rt);
        }
    }
}
