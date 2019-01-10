package com.github.charlemaznable.lang;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.github.charlemaznable.lang.Clz.isAssignable;

public class Typee {

    public static Class<?> getActualTypeArgument(
            Class<?> subClass,
            Class<?> genericInterface) {
        return getActualTypeArgument(subClass, genericInterface, 0);
    }

    public static Class<?> getActualTypeArgument(
            Class<?> subClass,
            Class<?> genericInterface,
            int argumentOrder) {
        for (Type generic : subClass.getGenericInterfaces()) {
            if (!(generic instanceof ParameterizedType)) continue;

            ParameterizedType pt = (ParameterizedType) generic;
            if (pt.getRawType() != genericInterface) continue;

            Type type = pt.getActualTypeArguments()[argumentOrder];
            return (Class<?>) type;
        }

        Class<?>[] interfaces = subClass.getInterfaces();
        for (Class<?> impInterface : interfaces) {
            if (isAssignable(impInterface, genericInterface)) {
                return getActualTypeArgument(
                        impInterface,
                        genericInterface,
                        argumentOrder);
            }
        }

        Class<?> superClz = subClass.getSuperclass();
        if (superClz == Object.class) return null;

        return getActualTypeArgument(superClz,
                genericInterface,
                argumentOrder);
    }
}
