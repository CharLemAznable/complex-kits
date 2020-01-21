package com.github.charlemaznable.core.net.ohclient.param;

import org.springframework.http.HttpStatus;

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
@Repeatable(OhStatusSeriesMappings.class)
public @interface OhStatusSeriesMapping {

    HttpStatus.Series statusSeries();

    Class<? extends RuntimeException> exception();
}
