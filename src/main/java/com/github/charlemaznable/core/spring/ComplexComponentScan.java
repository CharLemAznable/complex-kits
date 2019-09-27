package com.github.charlemaznable.core.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ComponentScan(nameGenerator = ComplexBeanNameGenerator.class)
public @interface ComplexComponentScan {

    @AliasFor(annotation = ComponentScan.class)
    String[] basePackages() default {};

    @AliasFor(annotation = ComponentScan.class)
    Class<?>[] basePackageClasses() default {};

    @AliasFor(annotation = ComponentScan.class)
    Filter[] includeFilters() default {};

    @AliasFor(annotation = ComponentScan.class)
    Filter[] excludeFilters() default {};
}
