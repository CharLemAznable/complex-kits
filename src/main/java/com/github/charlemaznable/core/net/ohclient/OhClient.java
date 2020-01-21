package com.github.charlemaznable.core.net.ohclient;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OhClient {

    @AliasFor("url")
    String value() default "";

    @AliasFor("value")
    String url() default "";

    Class<? extends UrlProvider> urlProvider() default UrlProvider.class;

    interface UrlProvider {

        String url(Class<?> clazz);
    }
}
