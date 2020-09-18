package com.github.charlemaznable.core.miner;

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
public @interface MinerStoneParse {

    Class<? extends MinerStoneParser> value();

    interface MinerStoneParser {

        Object parse(String stone, Class<?> clazz);
    }
}
