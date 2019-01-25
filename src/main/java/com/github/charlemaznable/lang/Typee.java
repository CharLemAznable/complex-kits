package com.github.charlemaznable.lang;

import lombok.val;

import java.lang.reflect.ParameterizedType;

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
        for (val generic : subClass.getGenericInterfaces()) {
            if (!(generic instanceof ParameterizedType)) continue;

            val pt = (ParameterizedType) generic;
            if (pt.getRawType() != genericInterface) continue;

            val type = pt.getActualTypeArguments()[argumentOrder];
            return (Class<?>) type;
        }

        val interfaces = subClass.getInterfaces();
        for (val impInterface : interfaces) {
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
