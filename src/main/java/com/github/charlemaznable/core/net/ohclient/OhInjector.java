package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.guice.CommonInjector;
import com.github.charlemaznable.core.guice.InjectorFactory;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.ohLoader;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public final class OhInjector extends CommonInjector {

    private OhLoader ohLoader;

    public OhInjector(Module... modules) {
        super(modules);
    }

    public OhInjector(Iterable<? extends Module> modules) {
        super(modules);
    }

    @Override
    public void initialize(InjectorFactory injectorFactory) {
        this.ohLoader = ohLoader(injectorFactory);
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
