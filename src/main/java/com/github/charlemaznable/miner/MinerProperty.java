package com.github.charlemaznable.miner;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinerProperty {

    String group() default "";

    @AliasFor("value")
    String dataId() default "";

    @AliasFor("dataId")
    String value() default "";

    String defaultValue() default "";
}
