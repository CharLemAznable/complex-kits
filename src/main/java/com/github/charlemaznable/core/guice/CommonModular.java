package com.github.charlemaznable.core.guice;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import lombok.val;
import org.springframework.util.ClassUtils;

import java.util.Set;
import java.util.function.Predicate;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.spring.ClzResolver.getClasses;
import static com.google.common.collect.Sets.newHashSet;

@SuppressWarnings("unchecked")
public abstract class CommonModular<M extends CommonModular> {

    protected final Module baseModule;
    protected final GuiceFactory guiceFactory;
    protected final Set<Class<?>> classes;
    private final Predicate<Class<?>> resolverPredicate = this::isCandidateClass;

    public CommonModular(Iterable<? extends Module> baseModules) {
        this.baseModule = Modulee.combine(baseModules);
        this.guiceFactory = new GuiceFactory(Guice.createInjector(this.baseModule));
        this.classes = newHashSet();
    }

    public abstract boolean isCandidateClass(Class<?> clazz);

    public abstract <T> Provider<T> createProvider(Class<T> clazz);

    public M bindClasses(Class<?>... classes) {
        return bindClasses(newArrayList(classes));
    }

    public M bindClasses(Iterable<Class<?>> classes) {
        this.classes.addAll(newArrayList(classes));
        return (M) this;
    }

    public M bindScanPackages(String... basePackages) {
        return bindScanPackages(newArrayList(basePackages));
    }

    public M bindScanPackages(Iterable<String> basePackages) {
        for (val basePackage : basePackages) {
            bindClasses(getClasses(basePackage, resolverPredicate));
        }
        return (M) this;
    }

    public M bindScanPackageClasses(Class<?>... basePackageClasses) {
        return bindScanPackageClasses(newArrayList(basePackageClasses));
    }

    public M bindScanPackageClasses(Iterable<Class<?>> basePackageClasses) {
        for (val basePackageClass : basePackageClasses) {
            val basePackage = ClassUtils.getPackageName(basePackageClass);
            bindClasses(getClasses(basePackage, resolverPredicate));
        }
        return (M) this;
    }

    public Module createModule() {
        val classSet = ImmutableSet.copyOf(classes);
        return Modulee.override(this.baseModule, new AbstractModule() {
            @Override
            protected void configure() {
                for (Class clazz : classSet) {
                    if (!clazz.isInterface() || !isCandidateClass(clazz)) {
                        bind(clazz).toProvider(Providers.of(null));
                    } else {
                        bindProviderTraverse(clazz, createProvider(clazz));
                    }
                }
            }

            private void bindProviderTraverse(Class clazz, Provider provider) {
                bind(clazz).toProvider(provider);
                val interfaces = clazz.getInterfaces();
                for (val interfacee : interfaces) {
                    if (isCandidateClass(interfacee)) {
                        bind(interfacee).toProvider(provider);
                    }
                }
            }
        });
    }
}
