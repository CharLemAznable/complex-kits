package com.github.charlemaznable.core.spring;

import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Clz.isAssignable;
import static com.github.charlemaznable.core.lang.ClzPath.findClass;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;
import static org.springframework.util.SystemPropertyUtils.resolvePlaceholders;

public final class ClzResolver {

    private static final String ALL_MATCH_PATTERN = "/**/*.";
    private static final String CLASS_PATTER = ALL_MATCH_PATTERN + "class";

    private ClzResolver() {}

    @SneakyThrows
    public static List<Class<?>> getClasses(String basePackage, Predicate<Class<?>> classPredicate) {
        var resolver = new PathMatchingResourcePatternResolver();
        var readerFactory = new CachingMetadataReaderFactory(resolver);
        var resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX
                + resolveBasePackage(basePackage) + CLASS_PATTER);

        return stream(resources).filter(Resource::isReadable)
                .map(resource -> resolveResourceClass(resource, readerFactory))
                .filter(clazz -> testResolvedClass(clazz, classPredicate)).collect(toList());
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

    @SneakyThrows
    public static List<URL> getResources(String basePackage, String extension) {
        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources(CLASSPATH_ALL_URL_PREFIX
                + resolveBasePackage(basePackage) + ALL_MATCH_PATTERN + extension);

        return stream(resources).filter(Resource::isReadable)
                .map(ClzResolver::resolveResourceURL).collect(toList());
    }

    private static String resolveBasePackage(String basePackage) {
        return convertClassNameToResourcePath(resolvePlaceholders(basePackage));
    }

    @SneakyThrows
    private static Class<?> resolveResourceClass(Resource resource,
                                                 MetadataReaderFactory readerFactory) {
        return findClass(readerFactory.getMetadataReader(resource)
                .getClassMetadata().getClassName());
    }

    private static boolean testResolvedClass(Class<?> clazz, Predicate<Class<?>> classPredicate) {
        return nonNull(clazz) && (isNull(classPredicate) || classPredicate.test(clazz));
    }

    @SneakyThrows
    private static URL resolveResourceURL(Resource resource) {
        return resource.getURL();
    }
}
