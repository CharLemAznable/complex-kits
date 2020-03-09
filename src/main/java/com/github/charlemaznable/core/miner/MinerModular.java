package com.github.charlemaznable.core.miner;

import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.core.miner.MinerFactory.MinerLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.miner.MinerFactory.minerLoader;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class MinerModular extends CommonModular {

    private MinerLoader minerLoader;

    public MinerModular(Module... modules) {
        super(modules);
    }

    public MinerModular(Iterable<? extends Module> modules) {
        super(modules);
    }

    @Override
    public void initialize(GuiceFactory guiceFactory) {
        this.minerLoader = minerLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class clazz) {
        return nonNull(getAnnotation(clazz, MinerConfig.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getMiner(clazz);
    }

    public <T> T getMiner(Class<T> minerClass) {
        return this.minerLoader.getMiner(minerClass);
    }
}
