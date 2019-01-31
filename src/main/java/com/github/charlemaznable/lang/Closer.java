package com.github.charlemaznable.lang;

import lombok.val;

import java.io.Closeable;

import static com.github.charlemaznable.lang.Clz.getMethod;
import static com.github.charlemaznable.lang.Clz.invokeQuietly;

public class Closer {

    /**
     * 关闭对象, 屏蔽所有异常。
     * 调用对象的close方法（如果对象有该方法的话）。
     *
     * @param objs 对象列表
     */
    public static void closeQuietly(Object... objs) {
        for (val obj : objs) {
            closeQuietly(obj);
        }
    }

    /**
     * 关闭对象, 屏蔽所有异常。
     *
     * @param obj 待关闭对象
     */
    public static void closeQuietly(Object obj) {
        if (obj == null) return;

        if (obj instanceof Closeable) {
            try {
                ((Closeable) obj).close();
            } catch (Throwable ignored) {
            }
            return;
        }

        val method = getMethod(obj.getClass(), "close");
        if (method != null && method.getParameterTypes().length == 0) {
            invokeQuietly(obj, method);
        }
    }
}
