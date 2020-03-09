package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VertxInjector {

    private Module vertxModule;

    public VertxInjector() {
        this((VertxOptions) null);
    }

    public VertxInjector(VertxOptions vertxOptions) {
        this(new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(Providers.of(vertxOptions));
                bind(Vertx.class).toProvider(VertxProvider.class);
            }
        });
    }

    public VertxInjector(Class<? extends Provider<VertxOptions>> vertxOptionsProviderClass) {
        this(new AbstractModule() {
            @Override
            protected void configure() {
                bind(VertxOptions.class).toProvider(vertxOptionsProviderClass);
                bind(Vertx.class).toProvider(VertxProvider.class);
            }
        });
    }

    public Module createModule() {
        return this.vertxModule;
    }

    public Injector createInjector() {
        return Guice.createInjector(createModule());
    }
}
