package com.github.charlemaznable.core.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

import java.util.Arrays;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;

public abstract class CommonInjector {

    protected InjectorFactory injectorFactory;

    public CommonInjector(Module... modules) {
        this(Arrays.asList(modules));
    }

    public CommonInjector(Iterable<? extends Module> modules) {
        this(Guice.createInjector(modules));
    }

    public CommonInjector(Injector injector) {
        this.injectorFactory = new InjectorFactory(checkNotNull(injector));
        initialize(this.injectorFactory);
    }

    public abstract void initialize(InjectorFactory injectorFactory);

    public abstract <T> Provider<T> createProvider(Class<T> clazz);

    public Module createModule(Class... ohClasses) {
        return createModule(Arrays.asList(ohClasses));
    }

    public Module createModule(Iterable<Class> ohClasses) {
        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (Class minerClass : ohClasses) {
                    bind(minerClass).toProvider(
                            createProvider(minerClass));
                }
            }
        };
    }

    public Injector createInjector(Class... ohClasses) {
        return createInjector(Arrays.asList(ohClasses));
    }

    public Injector createInjector(Iterable<Class> ohClasses) {
        return Guice.createInjector(createModule(ohClasses));
    }
}
