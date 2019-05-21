package com.github.charlemaznable.miner;

import com.github.charlemaznable.lang.EasyEnhancer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.Minerable;

import java.lang.reflect.Method;

import static com.github.charlemaznable.lang.Condition.blankThen;
import static com.github.charlemaznable.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.lang.Condition.checkNotNull;
import static com.github.charlemaznable.lang.Str.toStr;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

@SuppressWarnings("unchecked")
@UtilityClass
public class MinerFactory {

    private Cache<Class, String> stoneCache = CacheBuilder.newBuilder().build();
    private Cache<Class, Object> minerCache = CacheBuilder.newBuilder().build();

    @SneakyThrows
    public <T> String getStone(final Class<T> minerClass) {
        ensureClassIsAnInterface(minerClass);
        val minerConfig = checkMinerConfig(minerClass);
        return stoneCache.get(minerClass, () -> loadStone(minerConfig));
    }

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
        val minerConfig = checkNotNull(findAnnotation(clazz, MinerConfig.class),
                new MinerConfigException(clazz + " has no MinerConfig"));
        checkNotBlank(minerConfig.group(),
                new MinerConfigException(clazz + " MinerConfig group is Blank"));
        checkNotBlank(minerConfig.dataId(),
                new MinerConfigException(clazz + " MinerConfig dataId is Blank"));
        return minerConfig;
    }

    private String loadStone(MinerConfig minerConfig) {
        return new Miner().getStone(minerConfig.group(), minerConfig.dataId());
    }

    @SneakyThrows
    private Object loadMiner(Class minerClass, MinerConfig minerConfig) {
        return EasyEnhancer.create(Object.class, new Class[]{minerClass},
                new MinerProxy(minerConfig.group(), minerConfig.dataId()), null);
    }

    private static class MinerProxy implements MethodInterceptor {

        private Minerable minerable;

        public MinerProxy(String group, String dataId) {
            this.minerable = new Miner().getMiner(group, dataId);
        }

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            if (method.getDeclaringClass().equals(Minerable.class)) {
                return method.invoke(minerable, args);
            }

            val minerProperty = findAnnotation(method, MinerProperty.class);
            val group = null != minerProperty ? minerProperty.group() : "";
            val dataId = null != minerProperty ? minerProperty.dataId() : "";
            var defaultValue = args.length > 0 && null != args[0] ? toStr(args[0])
                    : null != minerProperty ? minerProperty.defaultValue() : "";
            return minerable.getStone(group,
                    blankThen(dataId, method::getName),
                    blankThen(defaultValue, () -> null));
        }
    }
}
