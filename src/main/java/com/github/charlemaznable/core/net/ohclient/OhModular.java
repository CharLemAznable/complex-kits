package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.ohLoader;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class OhModular extends CommonModular {

    private OhLoader ohLoader;

    public OhModular(Module... modules) {
        super(modules);
    }

    public OhModular(Iterable<? extends Module> modules) {
        super(modules);
    }

    @Override
    public void initialize(GuiceFactory guiceFactory) {
        this.ohLoader = ohLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class clazz) {
        return nonNull(getAnnotation(clazz, OhClient.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getClient(clazz);
    }

    public <T> T getClient(Class<T> ohClass) {
        return this.ohLoader.getClient(ohClass);
    }
}
