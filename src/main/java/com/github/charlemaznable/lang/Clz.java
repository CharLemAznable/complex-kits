package com.github.charlemaznable.lang;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.reflect.Modifier.isAbstract;

public class Clz {

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
     * @param m      方法
     * @return 方法返回
     */
    public static Object invokeQuietly(Object target, Method m) {
        try {
            return m.invoke(target);
        } catch (IllegalArgumentException | IllegalAccessException ignored) {
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            if (e.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) e.getTargetException();
            }
        }

        return null;
    }
}
