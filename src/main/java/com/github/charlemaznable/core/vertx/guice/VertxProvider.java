package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import javax.annotation.Nullable;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.vertx.VertxElf.buildVertx;

public final class VertxProvider implements Provider<Vertx> {

    private final VertxOptions vertxOptions;

    @Inject
    public VertxProvider(@Nullable VertxOptions vertxOptions) {
        this.vertxOptions = nullThen(vertxOptions, VertxOptions::new);
    }

    @Override
    public Vertx get() {
        return buildVertx(vertxOptions);
    }
}
