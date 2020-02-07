package com.github.charlemaznable.core.spring;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nonnull;
import java.util.function.Function;

@RequiredArgsConstructor
public class SpringFactoryBean implements FactoryBean, ApplicationContextAware {

    private final Function<Class, Object> factory;
    @Setter
    private Class xyzInterface;
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

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        SpringContext.updateApplicationContext(applicationContext);
    }
}
