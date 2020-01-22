package com.github.charlemaznable.core.net.ohclient.param;

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
@Repeatable(OhFixedContexts.class)
public @interface OhFixedContext {

    String name();

    String value() default "";

    Class<? extends OhFixedValueProvider> valueProvider() default OhFixedValueProvider.class;
}