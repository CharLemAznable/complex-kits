package com.github.charlemaznable.lang.pool;

import lombok.val;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import static com.github.charlemaznable.lang.Condition.checkNotNull;
import static com.github.charlemaznable.lang.Typee.getActualTypeArgument;
import static org.joor.Reflect.onClass;

public abstract class PooledObjectCreator<T> {

    @SuppressWarnings("RedundantThrows")
    public T create(Object... args) throws Exception {
        val tType = getActualTypeArgument(this.getClass(), PooledObjectCreator.class);
        checkNotNull(tType, "PooledObjectCreator's Type Argument is Missing");
        return onClass(tType).create(args).get();
    }

    public PooledObject<T> wrap(T t) {
        return new DefaultPooledObject<>(t);
    }
}
