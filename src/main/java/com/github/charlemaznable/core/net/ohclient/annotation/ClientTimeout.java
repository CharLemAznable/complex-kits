package com.github.charlemaznable.core.net.ohclient.annotation;

import com.github.charlemaznable.core.net.common.ProviderException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CALL_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_CONNECT_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_READ_TIMEOUT;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.DEFAULT_WRITE_TIMEOUT;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientTimeout {

    long callTimeout() default DEFAULT_CALL_TIMEOUT;

    long connectTimeout() default DEFAULT_CONNECT_TIMEOUT;

    long readTimeout() default DEFAULT_READ_TIMEOUT;

    long writeTimeout() default DEFAULT_WRITE_TIMEOUT;

    Class<? extends TimeoutProvider> callTimeoutProvider() default TimeoutProvider.class;

    Class<? extends TimeoutProvider> connectTimeoutProvider() default TimeoutProvider.class;

    Class<? extends TimeoutProvider> readTimeoutProvider() default TimeoutProvider.class;

    Class<? extends TimeoutProvider> writeTimeoutProvider() default TimeoutProvider.class;

    interface TimeoutProvider {

        default long timeout(Class<?> clazz) {
            throw new ProviderException(this.getClass().getName()
                    + "#timeout(Class<?>) need be overwritten");
        }

        default long timeout(Class<?> clazz, Method method) {
            throw new ProviderException(this.getClass().getName()
                    + "#timeout(Class<?>, Method) need be overwritten");
        }
    }
}
