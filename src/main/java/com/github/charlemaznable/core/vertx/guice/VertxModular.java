package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import static com.google.inject.Scopes.SINGLETON;

public class VertxModular {

    public Module createModule() {
        return createModule((VertxOptions) null);
    }

    public Module createModule(VertxOptions vertxOptions) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(Providers.of(vertxOptions));
                bind(Vertx.class).toProvider(VertxProvider.class).in(SINGLETON);
            }
        };
    }

    public Module createModule(Class<? extends Provider<VertxOptions>> vertxOptionsProviderClass) {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(vertxOptionsProviderClass);
                bind(Vertx.class).toProvider(VertxProvider.class).in(SINGLETON);
            }
        };
    }
}
