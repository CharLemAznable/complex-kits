package com.github.charlemaznable.core.net.ohclient.param;

import com.github.charlemaznable.core.net.ohclient.exception.OhError;
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
@Repeatable(OhStatusMappings.class)
public @interface OhStatusMapping {

    HttpStatus status();

    Class<? extends OhError> exception() default OhError.class;
}
