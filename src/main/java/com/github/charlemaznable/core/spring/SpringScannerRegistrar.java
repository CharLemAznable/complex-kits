package com.github.charlemaznable.core.spring;

import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.util.ArrayList;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class SpringScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private final Class<? extends Annotation> scanAnnotationClass;
    private final Class factoryBeanClass;
    private final Class<? extends Annotation>[] annotationClass;
    private ResourceLoader resourceLoader;

    @SafeVarargs
    public SpringScannerRegistrar(Class<? extends Annotation> scanAnnotationClass,
                                  Class factoryBeanClass,
                                  Class<? extends Annotation>... annotationClass) {
        this.scanAnnotationClass = scanAnnotationClass;
        this.factoryBeanClass = factoryBeanClass;
        this.annotationClass = annotationClass;
    }

    @Override
    public final void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata,
                                              @Nonnull BeanDefinitionRegistry registry,
                                              BeanNameGenerator importBeanNameGenerator) {
        this.registerBeanDefinitions(importingClassMetadata, registry);
    }

    @Override
    public final void registerBeanDefinitions(@Nonnull AnnotationMetadata importingClassMetadata,
                                              @Nonnull BeanDefinitionRegistry registry) {
        val annoAttrs = AnnotationAttributes.fromMap(importingClassMetadata
                .getAnnotationAttributes(scanAnnotationClass.getName()));
        if (isNull(annoAttrs)) return;

        val scanner = new SpringClassPathScanner(
                registry, factoryBeanClass, this::isCandidateClass, annotationClass);
        if (nonNull(resourceLoader)) { // this check is needed in Spring 3.1
            scanner.setResourceLoader(resourceLoader);
        }

        Class<? extends BeanNameGenerator> generatorClass = annoAttrs.getClass("nameGenerator");
        if (!BeanNameGenerator.class.equals(generatorClass)) {
            scanner.setBeanNameGenerator(BeanUtils.instantiateClass(generatorClass));
        }

        val basePackages = new ArrayList<String>();
        for (val pkg : annoAttrs.getStringArray("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (val pkg : annoAttrs.getStringArray("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (val clazz : annoAttrs.getClassArray("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            val className = importingClassMetadata.getClassName();
            basePackages.add(ClassUtils.getPackageName(className));
        }

        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(basePackages));
    }

    @Override
    public final void setResourceLoader(@Nonnull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return true;
    }
}
