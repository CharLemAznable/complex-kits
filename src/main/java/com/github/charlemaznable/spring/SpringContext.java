package com.github.charlemaznable.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;

import static com.github.charlemaznable.lang.Str.isEmpty;

public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static <T> T getBean(String beanName) {
        return getBean(beanName, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName, T defaultValue) {
        if (applicationContext == null) return defaultValue;
        if (isEmpty(beanName)) return defaultValue;

        try {
            return (T) applicationContext.getBean(beanName);
        } catch (NoSuchBeanDefinitionException ignored) {
        }
        return defaultValue;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(Class<T> clazz, T defaultValue) {
        if (applicationContext == null) return defaultValue;
        if (clazz == null) return defaultValue;

        try {
            return applicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException ignored) {
        }
        return defaultValue;
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
