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
public @interface ExtraUrlQuery {

    Class<? extends ExtraUrlQueryBuilder> value();

    interface ExtraUrlQueryBuilder {

        String build(@Nonnull final Map<String, Object> parameterMap,
                     @Nonnull final Map<String, Object> contextMap);
    }
}
