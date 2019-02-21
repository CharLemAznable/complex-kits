package com.github.charlemaznable.lang.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public abstract class PooledObjectCreator<T> {

    public abstract T create(Object... args) throws Exception;

    public PooledObject<T> wrap(T t) {
        return new DefaultPooledObject<>(t);
    }
}
