package com.github.charlemaznable.core.spring;

import lombok.val;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.Assert;

public class ComplexBeanNameGenerator extends AnnotationBeanNameGenerator {

    @SuppressWarnings("NullableProblems")
    @Override
    protected String buildDefaultBeanName(BeanDefinition definition) {
        val beanClassName = definition.getBeanClassName();
        Assert.state(beanClassName != null, "No bean class name set");
        return beanClassName;
    }
}
