package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.ohLoader;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class OhModular extends CommonModular<OhModular> {

    private OhLoader ohLoader;

    public OhModular(Module... modules) {
        this(newArrayList(modules));
    }

    public OhModular(Iterable<? extends Module> modules) {
        super(modules);
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
