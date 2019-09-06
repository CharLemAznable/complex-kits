package com.github.charlemaznable.core.spring;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.ClzPath.findClass;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;
import static org.springframework.util.SystemPropertyUtils.resolvePlaceholders;

public class ClzResolver {

    private static final String PATTERN = "/**/*.class";

    @SneakyThrows
    public static List<Class<?>> getClasses(String basePackage, Predicate<Class<?>> classPredicate) {
        val resolver = new PathMatchingResourcePatternResolver();
        val metaFactory = new CachingMetadataReaderFactory(resolver);

        val resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX
                + resolveBasePackage(basePackage) + PATTERN);

        List<Class<?>> classes = newArrayList();
        for (val res : resources) {
            if (!res.isReadable()) continue;

            val clazz = findClass(metaFactory.getMetadataReader(res)
                    .getClassMetadata().getClassName());
            if (null == clazz) continue;
            if (null == classPredicate || classPredicate.test(clazz)) classes.add(clazz);
        }
        return classes;
    }

    public static List<Class<?>> getClasses(String basePackage) {
        return getClasses(basePackage, null);
    }

    public static List<Class<?>> getSubClasses(String basePackage, Class<?> superClass) {
        return getClasses(basePackage, clazz -> isAssignable(clazz, superClass) && !clazz.equals(superClass));
    }

    public static List<Class<?>> getAnnotatedClasses(String basePackage, Class<? extends Annotation> annoClass) {
        return getClasses(basePackage, clazz -> clazz.isAnnotationPresent(annoClass));
    }

    private static String resolveBasePackage(String basePackage) {
        return convertClassNameToResourcePath(resolvePlaceholders(basePackage));
    }
}
