package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.guice.CommonInjector;
import com.github.charlemaznable.core.guice.InjectorFactory;
import com.github.charlemaznable.core.miner.MinerFactory.MinerLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.miner.MinerFactory.minerLoader;
import static java.util.Objects.isNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class MinerInjector extends CommonInjector {

    private MinerLoader minerLoader;

    public MinerInjector(Module... modules) {
        super(modules);
    }

    public MinerInjector(Iterable<? extends Module> modules) {
        super(modules);
    }

    @Override
    public void initialize(InjectorFactory injectorFactory) {
        this.minerLoader = minerLoader(injectorFactory);
    }

    @Override
    public boolean isNonCandidateClass(Class clazz) {
        return isNull(getAnnotation(clazz, MinerConfig.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getMiner(clazz);
    }

    public <T> T getMiner(Class<T> minerClass) {
        return this.minerLoader.getMiner(minerClass);
    }
}
