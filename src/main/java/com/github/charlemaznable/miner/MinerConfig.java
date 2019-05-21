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
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinerConfig {

    String group() default "DEFAULT_GROUP";

    @AliasFor("value")
    String dataId() default "DEFAULT_DATA";

    @AliasFor("dataId")
    String value() default "DEFAULT_DATA";

    boolean createClassFileForDiagnose() default false;
}
