package com.github.charlemaznable.core.vertx.guice;

import com.github.charlemaznable.core.guice.Modulee;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.AllArgsConstructor;

import static com.google.inject.Scopes.SINGLETON;

@AllArgsConstructor
public final class VertxModular {

    private final Module vertxOptionsModule;

    public VertxModular() {
        this((VertxOptions) null);
    }

    public VertxModular(VertxOptions vertxOptions) {
        this(new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(Providers.of(vertxOptions));
            }
        });
    }

    public VertxModular(Class<? extends Provider<VertxOptions>> vertxOptionsProviderClass) {
        this(new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(vertxOptionsProviderClass);
            }
        });
    }

    public Module createModule() {
        return Modulee.combine(vertxOptionsModule, new AbstractModule() {
            @Override
            protected void configure() {
                bind(Vertx.class).toProvider(VertxProvider.class).in(SINGLETON);
            }
        });
    }
}
