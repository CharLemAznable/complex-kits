package com.github.charlemaznable.core.miner;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinerConfig {

    /**
     * default "DEFAULT_GROUP"  when annotated on ElementType.TYPE
     * default ""               when annotated on ElementType.METHOD
     */
    String group() default "";

    @AliasFor("value")
    String dataId() default "";

    @AliasFor("dataId")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    Class<? extends GroupProvider> groupProvider() default GroupProvider.class;

    Class<? extends DataIdProvider> dataIdProvider() default DataIdProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface GroupProvider {

        String group(Class<?> minerClass, Method method);
    }

    interface DataIdProvider {

        String dataId(Class<?> minerClass, Method method);
    }

    interface DefaultValueProvider {

        String defaultValue(Class<?> minerClass, Method method);
    }
}
