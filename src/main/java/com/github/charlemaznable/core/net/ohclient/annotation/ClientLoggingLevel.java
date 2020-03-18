package com.github.charlemaznable.core.net.ohclient.annotation;

import com.github.charlemaznable.core.net.common.ProviderException;
import okhttp3.logging.HttpLoggingInterceptor.Level;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientLoggingLevel {

    Level value() default Level.NONE;

    Class<? extends LoggingLevelProvider> provider() default LoggingLevelProvider.class;

    interface LoggingLevelProvider {

        default Level level(Class<?> clazz) {
            throw new ProviderException(this.getClass().getName()
                    + "#level(Class<?>) need be overwritten");
        }

        default Level level(Class<?> clazz, Method method) {
            throw new ProviderException(this.getClass().getName()
                    + "#level(Class<?>, Method) need be overwritten");
        }
    }
}
