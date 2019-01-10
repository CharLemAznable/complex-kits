package com.github.charlemaznable.lang.pool;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.reflect.Method;

/**
 * 对象池代理工具
 * cglib实现代理需要池对象包含默认构造函数.
 */
public class PoolProxy {

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T create(@NonNull ObjectPool<T> pool) {
        T poolObject = null;
        try {
            poolObject = pool.borrowObject();
            return (T) Enhancer.create(poolObject.getClass(),
                    new Class[]{}, new ObjectPoolProxy<>(pool));
        } finally {
            if (poolObject != null) pool.returnObject(poolObject);
        }
    }

    public static <T> T create(@NonNull PooledObjectCreator<T> creator) {
        return create(creator, null);
    }

    public static <T> T create(@NonNull PooledObjectCreator<T> creator,
                               GenericObjectPoolConfig<T> config) {
        PoolProxyPooledObjectFactory<T> factory = new PoolProxyPooledObjectFactory<>(creator);
        return create(config == null ? new GenericObjectPool<>(factory)
                : new GenericObjectPool<>(factory, config));
    }

    /**
     * 对象池代理
     * <p>
     * 从对象池取出对象完成任务
     * ==
     * 调用代理对象完成任务, 即由代理完成 [从对象池取出对象]->完成任务->[向对象池交还对象]
     */
    @AllArgsConstructor
    private static class ObjectPoolProxy<T> implements MethodInterceptor {

        private ObjectPool<T> pool;

        @Override
        public Object intercept(Object o, Method method, Object[] args,
                                MethodProxy methodProxy) throws Throwable {
            T poolObject = null;
            try {
                poolObject = pool.borrowObject();
                return method.invoke(poolObject, args);
            } finally {
                if (poolObject != null)
                    pool.returnObject(poolObject);
            }
        }
    }

    /**
     * 池化对象工厂封装
     */
    @AllArgsConstructor
    private static class PoolProxyPooledObjectFactory<T> extends BasePooledObjectFactory<T> {

        private PooledObjectCreator<T> pooledObjectCreator;

        @Override
        public T create() throws Exception {
            return pooledObjectCreator.create();
        }

        @Override
        public PooledObject<T> wrap(T t) {
            return pooledObjectCreator.wrap(t);
        }
    }
}
