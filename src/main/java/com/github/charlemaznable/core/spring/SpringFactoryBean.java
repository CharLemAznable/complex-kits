package com.github.charlemaznable.core.spring;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.function.Function;

@RequiredArgsConstructor
public class SpringFactoryBean implements FactoryBean, ApplicationContextAware {

    private final Function<Class, Object> factory;
    @Setter
    private Class xyzInterface;
    @Setter
    private ApplicationContext applicationContext;

    @Override
    public Object getObject() {
        val activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        ActiveProfilesThreadLocal.set(activeProfiles);
        return factory.apply(xyzInterface);
    }

    @Override
    public Class<?> getObjectType() {
        return this.xyzInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
