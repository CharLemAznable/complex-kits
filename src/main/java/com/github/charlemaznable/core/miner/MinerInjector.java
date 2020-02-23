package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.miner.MinerFactory.MinerLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.Arrays;

import static com.github.charlemaznable.core.lang.Condition.checkNotNull;

public class MinerInjector {

    private final Injector injector;
    private final MinerLoader minerLoader;

    public MinerInjector(Module... modules) {
        this(Arrays.asList(modules));
    }

    public MinerInjector(Iterable<? extends Module> modules) {
        this(Guice.createInjector(modules));
    }

    public MinerInjector(Injector injector) {
        this.injector = checkNotNull(injector);
        this.minerLoader = MinerFactory.minerLoader(this.injector::getInstance);
    }

    public Injector injectMiner(Class... minerClasses) {
        return injectMiner(Arrays.asList(minerClasses));
    }

    public Injector injectMiner(Iterable<Class> minerClasses) {
        return this.injector.createChildInjector(new AbstractModule() {
            @SuppressWarnings("unchecked")
            @Override
            protected void configure() {
                for (Class minerClass : minerClasses) {
                    bind(minerClass).toProvider(() ->
                            minerLoader.getMiner(minerClass));
                }
            }
        });
    }

    public <T> T getMiner(Class<T> minerClass) {
        return this.minerLoader.getMiner(minerClass);
    }
}
