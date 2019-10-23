package com.github.charlemaznable.core.spring;

import lombok.Synchronized;
import lombok.val;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.function.Supplier;

import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.github.charlemaznable.core.spring.ComplexBeanNameGenerator.getBeanClassName;
import static org.joor.Reflect.onClass;

@SuppressWarnings("unchecked")
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static DefaultListableBeanFactory defaultListableBeanFactory;

    public static <T> T getBean(String beanName) {
        return getBean(beanName, (T) null);
    }

    public static <T> T getBean(String beanName, T defaultValue) {
        return getBean(beanName, () -> defaultValue);
    }

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

    public static <T> T getBeanOrReflect(Class<T> clazz) {
        return getBean(clazz, (Supplier<T>) () -> onClass(clazz).create().get());
    }

    public static <T> T getBeanOrCreate(Class<T> clazz) {
        return getBean(clazz, (Supplier<T>) () -> createBean(clazz));
    }

    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz, (T) null);
    }

    public static <T> T getBean(Class<T> clazz, T defaultValue) {
        return getBean(clazz, (Supplier<T>) () -> defaultValue);
    }

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

    public static <T> T getBeanOrReflect(String beanName, Class<T> clazz) {
        return getBean(beanName, clazz, (Supplier<T>) () -> onClass(clazz).create().get());
    }

    public static <T> T getBeanOrCreate(String beanName, Class<T> clazz) {
        return getBean(beanName, clazz, (Supplier<T>) () -> createBean(beanName, clazz));
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getBean(beanName, clazz, (T) null);
    }

    public static <T> T getBean(String beanName, Class<T> clazz, T defaultValue) {
        return getBean(beanName, clazz, (Supplier<T>) () -> defaultValue);
    }

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

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T createBean(Class<T> clazz) {
        val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(clazz).getBeanDefinition();
        defaultListableBeanFactory.registerBeanDefinition(
                getBeanClassName(beanDefinition), beanDefinition);
        defaultListableBeanFactory.clearMetadataCache();
        return getBean(clazz);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T createBean(String beanName, Class<T> clazz) {
        val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(clazz).getBeanDefinition();
        defaultListableBeanFactory.registerBeanDefinition(
                beanName, beanDefinition);
        defaultListableBeanFactory.clearMetadataCache();
        return getBean(beanName, clazz);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T autowireBean(T bean) {
        val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(bean.getClass()).getBeanDefinition();
        return autowireBean(getBeanClassName(beanDefinition), bean);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T autowireBean(String beanName, T bean) {
        defaultListableBeanFactory.autowireBean(bean);
        defaultListableBeanFactory.registerSingleton(beanName, bean);
        return bean;
    }

    @Synchronized
    private static void updateApplicationContext(@Nonnull ApplicationContext context) {
        applicationContext = context;
        defaultListableBeanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext context) {
        updateApplicationContext(context);
    }
}
