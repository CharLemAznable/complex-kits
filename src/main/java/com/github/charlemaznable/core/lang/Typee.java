package com.github.charlemaznable.core.lang;

import lombok.val;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;

public final class Typee {

    private Typee() {}

    public static Class<?> getActualTypeArgument(
            Class<?> subClass,
            Class<?> genericType) {
        return getActualTypeArgument(subClass, genericType, 0);
    }

    public static Class<?> getActualTypeArgument(
            Class<?> subClass,
            Class<?> genericType,
            int argumentOrder) {
        val genericSuperclass = subClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericSuperclass;
            val rawType = pt.getRawType();

            if (rawType == genericType || isAssignable(
                    (Class<?>) rawType, genericType)) {
                val type = pt.getActualTypeArguments()[argumentOrder];
                return (Class<?>) type;
            }
        }

        for (Type genericInterface : subClass.getGenericInterfaces()) {
            if (!(genericInterface instanceof ParameterizedType)) continue;

            ParameterizedType pt = (ParameterizedType) genericInterface;
            val rawType = pt.getRawType();

            if (rawType == genericType || isAssignable(
                    (Class<?>) rawType, genericType)) {
                val type = pt.getActualTypeArguments()[argumentOrder];
                return (Class<?>) type;
            }
        }

        val interfaces = subClass.getInterfaces();
        for (val impInterface : interfaces) {
            if (isAssignable(impInterface, genericType)) {
                return getActualTypeArgument(impInterface, genericType, argumentOrder);
            }
        }

        Class<?> superClz = subClass.getSuperclass();
        if (superClz == Object.class) return null;

        return getActualTypeArgument(superClz, genericType, argumentOrder);
    }
}
