package com.github.charlemaznable.core.spring;

import lombok.NoArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.nonNull;

public class SpringContextRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public final void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata,
                                              @Nonnull BeanDefinitionRegistry registry,
                                              BeanNameGenerator importBeanNameGenerator) {
        this.registerBeanDefinitions(importingClassMetadata, registry);
    }

    @Override
    public final void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata,
                                              @Nonnull BeanDefinitionRegistry registry) {
        val scanner = new ComplexScanner(registry);
        // this check is needed in Spring 3.1
        if (nonNull(resourceLoader)) scanner.setResourceLoader(resourceLoader);
        scanner.setBeanNameGenerator(new ComplexBeanNameGenerator());
        scanner.addIncludeFilter(new AssignableTypeFilter(ComplexDummy.class));
        scanner.doScan(ClassUtils.getPackageName(ComplexDummy.class));
    }

    @Override
    public final void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @NoArgsConstructor
    static final class ComplexDummy {}

    static class ComplexScanner extends ClassPathBeanDefinitionScanner {

        public ComplexScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Nonnull
        @Override
        public Set<BeanDefinitionHolder> doScan(String... basePackages) {
            val beanDefinitions = super.doScan(basePackages);
            for (val holder : beanDefinitions) {
                val definition = (GenericBeanDefinition) holder.getBeanDefinition();
                definition.getPropertyValues().add("xyzInterface", definition.getBeanClassName());
                definition.setBeanClass(ComplexFactoryBean.class);
            }
            return beanDefinitions;
        }
    }

    static class ComplexFactoryBean extends SpringFactoryBean {

        ComplexFactoryBean() {
            super(x -> new ComplexDummy());
        }
    }
}
