package com.github.charlemaznable.lang;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Clz {

    public static boolean isAssignable(Class<?> fromClass, Class<?>... toClasses) {
        for (Class<?> toClass : toClasses)
            if (ClassUtils.isAssignable(fromClass, toClass)) return true;

        return false;
    }

    public static boolean isConcrete(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Get method.
     *
     * @param class1     class
     * @param methodName method name
     * @return method
     */
    @SneakyThrows
    public static Method getMethod(Class<?> class1, String methodName) {
        return class1.getMethod(methodName);
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
