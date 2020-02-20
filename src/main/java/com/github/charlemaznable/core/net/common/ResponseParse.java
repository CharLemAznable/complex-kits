package com.github.charlemaznable.core.net.common;

import javax.annotation.Nonnull;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseParse {

    Class<? extends ResponseParser> value();

    interface ResponseParser {

        Object parse(@Nonnull final String responseContent,
                     @Nonnull final Class<?> returnType,
                     @Nonnull final Map<String, Object> contextMap);
    }
}
