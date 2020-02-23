package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.joor.Reflect.onClass;

public class OhInjector {

    private InjectorFactory injectorFactory;
    private OhLoader ohLoader;

    public OhInjector(Module... modules) {
        this(Arrays.asList(modules));
    }

    public OhInjector(Iterable<? extends Module> modules) {
        this(Guice.createInjector(modules));
    }

    public OhInjector(Injector injector) {
        this.injectorFactory = new InjectorFactory(checkNotNull(injector));
        this.ohLoader = OhFactory.ohLoader(this.injectorFactory);
    }

    public Module ohModule(Class... ohClasses) {
        return ohModule(Arrays.asList(ohClasses));
    }

    public Module ohModule(Iterable<Class> ohClasses) {
        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (Class minerClass : ohClasses) {
                    bind(minerClass).toProvider(() ->
                            ohLoader.getClient(minerClass));
                }
            }
        };
    }

    public Injector injectOhClient(Class... ohClasses) {
        return injectOhClient(Arrays.asList(ohClasses));
    }

    public Injector injectOhClient(Iterable<Class> ohClasses) {
        return this.injectorFactory.injector
                .createChildInjector(ohModule(ohClasses));
    }

    public <T> T getClient(Class<T> ohClass) {
        return this.ohLoader.getClient(ohClass);
    }

    @AllArgsConstructor
    private static class InjectorFactory implements Factory {

        private Injector injector;

        @Override
        public <T> T build(Class<T> clazz) {
            try {
                return injector.getInstance(clazz);
            } catch (Exception e) {
                return onClass(clazz).create().get();
            }
        }
    }
}
