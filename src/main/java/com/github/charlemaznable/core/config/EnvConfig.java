package com.github.charlemaznable.core.config;

import com.github.charlemaznable.core.config.ex.EnvConfigException;
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
public @interface EnvConfig {

    /**
     * effective when annotated on ElementType.METHOD
     */
    @AliasFor("value")
    String configKey() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    @AliasFor("configKey")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    Class<? extends ConfigKeyProvider> configKeyProvider() default ConfigKeyProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface ConfigKeyProvider {

        default String configKey(Class<?> minerClass, Method method) {
            throw new EnvConfigException(this.getClass().getName()
                    + "#configKey(Class<?>, Method) need be overwritten");
        }
    }

    interface DefaultValueProvider {

        default String defaultValue(Class<?> minerClass, Method method) {
            throw new EnvConfigException(this.getClass().getName()
                    + "#defaultValue(Class<?>, Method) need be overwritten");
        }
    }
}
