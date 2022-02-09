package com.github.charlemaznable.core.net.common;

import com.github.charlemaznable.core.net.ohclient.internal.OhFallbackFunction;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(StatusFallbacks.class)
public @interface StatusFallback {

    HttpStatus status();

    Class<? extends OhFallbackFunction> fallback();
}
