package com.github.charlemaznable.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static com.github.charlemaznable.lang.Condition.notNullThen;
import static com.github.charlemaznable.lang.Str.isEmpty;

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
        }
        return notNullThen(defaultSupplier, Supplier::get);
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
