package com.github.charlemaznable.core.guice;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import lombok.val;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public abstract class CommonInjector {

    protected Module baseModule;
    protected InjectorFactory injectorFactory;

    public CommonInjector(Module... baseModules) {
        this(newArrayList(baseModules));
    }

    public CommonInjector(Iterable<? extends Module> baseModules) {
        this.baseModule = Modulee.combine(newArrayList(baseModules));
        this.injectorFactory = new InjectorFactory(Guice.createInjector(this.baseModule));
        initialize(this.injectorFactory);
    }

    public abstract void initialize(InjectorFactory injectorFactory);

    public abstract boolean isCandidateClass(Class clazz);

    public abstract <T> Provider<T> createProvider(Class<T> clazz);

    public Module createModule(Class... classes) {
        return createModule(newArrayList(classes));
    }

    @SuppressWarnings("unchecked")
    public Module createModule(Iterable<Class> classes) {
        val classSet = ImmutableSet.copyOf(newArrayList(classes));
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

    public Injector createInjector(Class... classes) {
        return createInjector(newArrayList(classes));
    }

    public Injector createInjector(Iterable<Class> classes) {
        return Guice.createInjector(createModule(classes));
    }
}
