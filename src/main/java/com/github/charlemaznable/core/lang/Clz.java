package com.github.charlemaznable.core.lang;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static java.lang.reflect.Modifier.isAbstract;
import static org.joor.Reflect.wrapper;

public final class Clz {

    private Clz() {}

    public static boolean isAssignable(Class<?> fromClass, Class<?>... toClasses) {
        for (val toClass : toClasses)
            if (ClassUtils.isAssignable(fromClass, toClass)) return true;

        return false;
    }

    public static boolean isConcrete(Class<?> clazz) {
        return !clazz.isInterface() && !isAbstract(clazz.getModifiers());
    }

    /**
     * Get method.
     *
     * @param clazz      class
     * @param methodName method name
     * @return method
     */
    @SneakyThrows
    public static Method getMethod(Class<?> clazz, String methodName) {
        return clazz.getMethod(methodName);
    }

    /**
     * 安静的调用对象的方法。
     *
     * @param target 对象
     * @param method 方法
     * @param args   参数
     * @return 方法返回
     */
    public static Object invokeQuietly(Object target, Method method, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalArgumentException | IllegalAccessException ignored) {
            // ignored
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
        }

        return null;
    }

    public static Class<?>[] types(Object... values) {
        if (values == null) return new Class[0];

        val result = new Class[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = checkNull(values[i], () -> NULL.class, Object::getClass);
        }
        return result;
    }

    public static boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for (int i = 0; i < actualTypes.length; i++) {
                val actualType = actualTypes[i];
                if (actualType == NULL.class ||
                        wrapper(declaredTypes[i]).isAssignableFrom(
                                wrapper(actualType))) continue;
                return false;
            }
            return true;
        } else return false;
    }

    public static Class<?>[] getConstructorParameterTypes(Class<?> clazz, Object... arguments) {
        val types = types(arguments);

        try {
            return clazz.getDeclaredConstructor(types).getParameterTypes();
        } catch (NoSuchMethodException e) {
            for (val constructor : clazz.getDeclaredConstructors()) {
                val parameterTypes = constructor.getParameterTypes();
                if (match(parameterTypes, types)) return parameterTypes;
            }
            throw new IllegalArgumentException(clazz
                    + "'s Constructor with such arguments Not Found");
        }
    }

    private interface NULL {}
}
