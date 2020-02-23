package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.lang.Factory;
import com.github.charlemaznable.core.miner.MinerFactory.MinerLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.joor.Reflect.onClass;

public class MinerInjector {

    private InjectorFactory injectorFactory;
    private MinerLoader minerLoader;

    public MinerInjector(Module... modules) {
        this(Arrays.asList(modules));
    }

    public MinerInjector(Iterable<? extends Module> modules) {
        this(Guice.createInjector(modules));
    }

    public MinerInjector(Injector injector) {
        this.injectorFactory = new InjectorFactory(checkNotNull(injector));
        this.minerLoader = MinerFactory.minerLoader(this.injectorFactory);
    }

    public Module minerModule(Class... minerClasses) {
        return minerModule(Arrays.asList(minerClasses));
    }

    public Module minerModule(Iterable<Class> minerClasses) {
        return new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (Class minerClass : minerClasses) {
                    bind(minerClass).toProvider(() ->
                            minerLoader.getMiner(minerClass));
                }
            }
        };
    }

    public Injector injectMiner(Class... minerClasses) {
        return injectMiner(Arrays.asList(minerClasses));
    }

    public Injector injectMiner(Iterable<Class> minerClasses) {
        return this.injectorFactory.injector
                .createChildInjector(minerModule(minerClasses));
    }

    public <T> T getMiner(Class<T> minerClass) {
        return this.minerLoader.getMiner(minerClass);
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
