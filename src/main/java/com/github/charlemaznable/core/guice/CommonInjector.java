package com.github.charlemaznable.core.guice;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;
import lombok.val;

import java.util.Arrays;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public abstract class CommonInjector {

    protected Iterable<? extends Module> baseModules;
    protected InjectorFactory injectorFactory;

    public CommonInjector(Module... baseModules) {
        this(Arrays.asList(baseModules));
    }

    public CommonInjector(Iterable<? extends Module> baseModules) {
        this.baseModules = ImmutableSet.copyOf(newArrayList(baseModules));
        this.injectorFactory = new InjectorFactory(Guice.createInjector(this.baseModules));
        initialize(this.injectorFactory);
    }

    public abstract void initialize(InjectorFactory injectorFactory);

    public abstract boolean isNonCandidateClass(Class clazz);

    public abstract <T> Provider<T> createProvider(Class<T> clazz);

    public Module createModule(Class... classes) {
        return createModule(Arrays.asList(classes));
    }

    @SuppressWarnings("unchecked")
    public Module createModule(Iterable<Class> classes) {
        val classSet = ImmutableSet.copyOf(newArrayList(classes));
        return Modules.override(new AbstractModule() {
            @Override
            protected void configure() {
                for (Class clazz : classSet) {
                    if (isNonCandidateClass(clazz)) continue;
                    if (!clazz.isInterface()) {
                        bind(clazz).toProvider(Providers.of(null));
                    } else {
                        bindThenTraverse(clazz, createProvider(clazz));
                    }
                }
            }

            private void bindThenTraverse(Class clazz, Provider provider) {
                bind(clazz).toProvider(provider);
                val interfaces = clazz.getInterfaces();
                for (val interfacee : interfaces) {
                    if (isNonCandidateClass(interfacee)) continue;
                    bindThenTraverse(interfacee, provider);
                }
            }
        }).with(this.baseModules);
    }

    public Injector createInjector(Class... classes) {
        return createInjector(Arrays.asList(classes));
    }

    public Injector createInjector(Iterable<Class> classes) {
        return Guice.createInjector(createModule(classes));
    }
}
