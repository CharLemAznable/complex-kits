package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import org.springframework.core.annotation.AliasFor;

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
public @interface OhMapping {

    @AliasFor("url")
    String value() default "";

    @AliasFor("value")
    String url() default "";

    Class<? extends UrlProvider> urlProvider() default UrlProvider.class;

    interface UrlProvider {

        default String url(Class<?> clazz) {
            throw new OhException(this.getClass().getName()
                    + "#url(Class<?>) need be overwritten");
        }

        default String url(Class<?> clazz, Method method) {
            throw new OhException(this.getClass().getName()
                    + "#url(Class<?>, Method) need be overwritten");
        }
    }
}
