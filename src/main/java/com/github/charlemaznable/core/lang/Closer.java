package com.github.charlemaznable.core.lang;

import java.io.Closeable;

import static com.github.charlemaznable.core.lang.Clz.getMethod;
import static com.github.charlemaznable.core.lang.Clz.invokeQuietly;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public final class Closer {

    private Closer() {}

    /**
     * 关闭对象, 屏蔽所有异常。
     * 调用对象的close方法（如果对象有该方法的话）。
     *
     * @param objs 对象列表
     */
    public static void closeQuietly(Object... objs) {
        for (var obj : objs) {
            closeQuietly(obj);
        }
    }

    /**
     * 关闭对象, 屏蔽所有异常。
     *
     * @param obj 待关闭对象
     */
    public static void closeQuietly(Object obj) {
        if (isNull(obj)) return;

        if (obj instanceof Closeable) {
            try {
                ((Closeable) obj).close();
            } catch (Exception ignored) {
                // ignored
            }
            return;
        }

        var method = getMethod(obj.getClass(), "close");
        if (nonNull(method) && method.getParameterTypes().length == 0) {
            invokeQuietly(obj, method);
        }
    }
}
