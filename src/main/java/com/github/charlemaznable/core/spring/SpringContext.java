package com.github.charlemaznable.core.spring;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import java.util.function.UnaryOperator;

import static com.github.charlemaznable.core.lang.Clz.isConcrete;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Str.isEmpty;
import static com.github.charlemaznable.core.spring.ComplexBeanNameGenerator.getBeanClassName;
import static org.joor.Reflect.onClass;

@SuppressWarnings("unchecked")
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private static DefaultListableBeanFactory defaultListableBeanFactory;

    ////////////////////////////////////////////////////////////////

    public static <T> T getBeanOrAutowire(String beanName, T defaultValue) {
        // 默认值: 方法返回前注入上下文
        return getBeanOrAutowire(beanName, new FixedSupplier<>(defaultValue));
    }

    public static <T> T getBeanOrAutowire(String beanName, Supplier<T> defaultSupplier) {
        // 默认值: 方法返回前注入上下文
        return getBean(beanName, new WrapperSupplier<>(
                defaultSupplier, new AutowireWrapper<>(beanName)));
    }

    public static <T> T getBean(String beanName) {
        return getBean(beanName, (T) null);
    }

    public static <T> T getBean(String beanName, T defaultValue) {
        return getBean(beanName, new FixedSupplier<>(defaultValue));
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

    ////////////////////////////////////////////////////////////////

    public static <T> T getBeanOrReflect(Class<T> clazz) {
        // 默认值: 反射创建实例, 不注入上下文
        return getBean(clazz, new ReflectSupplier<>(clazz));
    }

    public static <T> T getBeanOrCreate(Class<T> clazz) {
        // 默认值: 由上下文创建实例, 同时注入
        //         若上下文不存在, 则反射创建实例
        return getBean(clazz, new CreateSupplier<>(clazz));
    }

    public static <T> T getBeanOrAutowire(Class<T> clazz, T defaultValue) {
        // 默认值: 方法返回前注入上下文
        //         若上下文不存在, 则直接返回
        return getBeanOrAutowire(clazz, new FixedSupplier<>(defaultValue));
    }

    public static <T> T getBeanOrAutowire(Class<T> clazz, Supplier<T> defaultSupplier) {
        // 默认值: 方法返回前注入上下文
        //         若上下文不存在, 则直接返回
        return getBean(clazz, new WrapperSupplier<>(
                defaultSupplier, new AutowireWrapper<>()));
    }

    public static <T> T getBean(Class<T> clazz) {
        return getBean(clazz, (T) null);
    }

    public static <T> T getBean(Class<T> clazz, T defaultValue) {
        return getBean(clazz, new FixedSupplier<>(defaultValue));
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

    ////////////////////////////////////////////////////////////////

    public static <T> T getBeanOrReflect(String beanName, Class<T> clazz) {
        // 默认值: 反射创建实例, 不注入上下文
        return getBean(beanName, clazz, new ReflectSupplier<>(clazz));
    }

    public static <T> T getBeanOrCreate(String beanName, Class<T> clazz) {
        // 默认值: 由上下文创建实例, 同时注入
        //         若上下文不存在, 则反射创建实例
        return getBean(beanName, clazz, new CreateSupplier<>(beanName, clazz));
    }

    public static <T> T getBeanOrAutowire(String beanName, Class<T> clazz, T defaultValue) {
        // 默认值: 方法返回前注入上下文
        //         若上下文不存在, 则直接返回
        return getBeanOrAutowire(beanName, clazz, new FixedSupplier<>(defaultValue));
    }

    public static <T> T getBeanOrAutowire(String beanName, Class<T> clazz, Supplier<T> defaultSupplier) {
        // 默认值: 方法返回前注入上下文
        //         若上下文不存在, 则直接返回
        return getBean(beanName, clazz, new WrapperSupplier<>(
                defaultSupplier, new AutowireWrapper<>(beanName)));
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getBean(beanName, clazz, (T) null);
    }

    public static <T> T getBean(String beanName, Class<T> clazz, T defaultValue) {
        return getBean(beanName, clazz, new FixedSupplier<>(defaultValue));
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

    ////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////

    public static <T> T createBean(Class<T> clazz) {
        return createBean(null, clazz);
    }

    public static <T> T createBean(String beanName, Class<T> clazz) {
        if (clazz == null) return null;
        if (applicationContext == null) {
            if (!isConcrete(clazz)) return null;
            return onClass(clazz).create().get();
        }

        val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(clazz).getBeanDefinition();
        val registerBeanName = nullThen(beanName,
                () -> getBeanClassName(beanDefinition));
        defaultListableBeanFactory.registerBeanDefinition(
                registerBeanName, beanDefinition);
        defaultListableBeanFactory.clearMetadataCache();
        return getBean(registerBeanName, clazz);
    }

    ////////////////////////////////////////////////////////////////

    @CanIgnoreReturnValue
    public static <T> T autowireBean(T bean) {
        return autowireBean(null, bean);
    }

    @CanIgnoreReturnValue
    public static <T> T autowireBean(String beanName, T bean) {
        if (applicationContext == null) return bean;
        if (bean == null) return null;

        val beanDefinition = BeanDefinitionBuilder
                .genericBeanDefinition(bean.getClass()).getBeanDefinition();
        val registerBeanName = nullThen(beanName,
                () -> getBeanClassName(beanDefinition));
        defaultListableBeanFactory.autowireBean(bean);
        defaultListableBeanFactory.registerSingleton(registerBeanName, bean);
        return bean;
    }

    ////////////////////////////////////////////////////////////////

    @Synchronized
    static void updateApplicationContext(@Nonnull ApplicationContext context) {
        applicationContext = context;
        defaultListableBeanFactory = (DefaultListableBeanFactory)
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
    }

    @Override
    public final void setApplicationContext(@Nonnull ApplicationContext context) {
        SpringContext.updateApplicationContext(context);
    }

    ////////////////////////////////////////////////////////////////

    @AllArgsConstructor
    static final class FixedSupplier<T> implements Supplier<T> {

        private T value;

        @Override
        public T get() {
            return value;
        }
    }

    @AllArgsConstructor
    static final class WrapperSupplier<T> implements Supplier<T> {

        private Supplier<T> supplier;
        private UnaryOperator<T> wrapper;

        @Override
        public T get() {
            return notNullThen(supplier, s -> checkNull(
                    wrapper, s, w -> w.apply(s.get())));
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    static final class AutowireWrapper<T> implements UnaryOperator<T> {

        private String beanName;

        @Override
        public T apply(T value) {
            return notNullThen(value, t -> autowireBean(beanName, t));
        }
    }

    @AllArgsConstructor
    static final class ReflectSupplier<T> implements Supplier<T> {

        private Class<T> clazz;

        @Override
        public T get() {
            return notNullThen(clazz, c -> {
                if (!isConcrete(c)) return null;
                return onClass(c).create().get();
            });
        }
    }

    @RequiredArgsConstructor
    @AllArgsConstructor
    static final class CreateSupplier<T> implements Supplier<T> {

        private String beanName;
        private final Class<T> clazz;

        @Override
        public T get() {
            return notNullThen(clazz, c -> createBean(beanName, c));
        }
    }
}
