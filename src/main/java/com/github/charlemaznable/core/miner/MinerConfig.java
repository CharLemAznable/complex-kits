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

    long cacheSeconds() default 0;

    Class<? extends GroupProvider> groupProvider() default GroupProvider.class;

    Class<? extends DataIdProvider> dataIdProvider() default DataIdProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface GroupProvider {

        default String group(Class<?> minerClass) {
            throw new MinerConfigException(this.getClass().getName()
                    + "#group(Class<?>) need be overwritten");
        }

        default String group(Class<?> minerClass, Method method) {
            throw new MinerConfigException(this.getClass().getName()
                    + "#group(Class<?>, Method) need be overwritten");
        }
    }

    interface DataIdProvider {

        default String dataId(Class<?> minerClass) {
            throw new MinerConfigException(this.getClass().getName()
                    + "#dataId(Class<?>) need be overwritten");
        }

        default String dataId(Class<?> minerClass, Method method) {
            throw new MinerConfigException(this.getClass().getName()
                    + "#dataId(Class<?>, Method) need be overwritten");
        }
    }

    interface DefaultValueProvider {

        default String defaultValue(Class<?> minerClass, Method method) {
            throw new MinerConfigException(this.getClass().getName()
                    + "#defaultValue(Class<?>, Method) need be overwritten");
        }
    }
}
