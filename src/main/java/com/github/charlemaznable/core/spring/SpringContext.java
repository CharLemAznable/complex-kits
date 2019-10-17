package com.github.charlemaznable.core.spring;

import lombok.Synchronized;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.isEmpty;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(String beanName) {
        return getBean(beanName, (T) null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, T defaultValue) {
        return getBean(beanName, () -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, Supplier<T> defaultSupplier) {
        if (applicationContext == null) return notNullThen(defaultSupplier, Supplier::get);
        if (isEmpty(beanName)) return notNullThen(defaultSupplier, Supplier::get);

        try {
            return (T) applicationContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException ignored) {
            // ignored
        }
        return notNullThen(defaultSupplier, Supplier::get);
    }

    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz, (T) null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz, T defaultValue) {
        return getBean(clazz, (Supplier<T>) () -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz, Supplier<T> defaultSupplier) {
        if (applicationContext == null) return notNullThen(defaultSupplier, Supplier::get);
        if (clazz == null) return notNullThen(defaultSupplier, Supplier::get);

        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException ignored) {
            // ignored
        }
        return notNullThen(defaultSupplier, Supplier::get);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getBean(beanName, clazz, (T) null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, Class<T> clazz, T defaultValue) {
        return getBean(beanName, clazz, (Supplier<T>) () -> defaultValue);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, Class<T> clazz, Supplier<T> defaultSupplier) {
        if (applicationContext == null) return notNullThen(defaultSupplier, Supplier::get);
        if (isEmpty(beanName) && clazz == null) return notNullThen(defaultSupplier, Supplier::get);
        if (isEmpty(beanName)) return getBean(clazz, defaultSupplier);
        if (clazz == null) return getBean(beanName, defaultSupplier);

        try {
            return applicationContext.getBean(beanName, clazz);
        } catch (NoSuchBeanDefinitionException ignored) {
            // ignored
        }
        return notNullThen(defaultSupplier, Supplier::get);
    }

    public static String[] getBeanNamesForType(Class<?> clazz) {
        if (applicationContext == null) return new String[0];
        if (clazz == null) return new String[0];
        return applicationContext.getBeanNamesForType(clazz);
    }

    public static String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotation) {
        if (applicationContext == null) return new String[0];
        if (annotation == null) return new String[0];
        return applicationContext.getBeanNamesForAnnotation(annotation);
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) {
        updateApplicationContext(context);
    }

    @Synchronized
    private static void updateApplicationContext(@Nonnull ApplicationContext context) {
        applicationContext = context;
    }
}
