package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Guice;
import com.google.inject.Provider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomOptionsTest {

    static final int DEFAULT_WORKER_POOL_SIZE = 42;
    static final VertxOptions vertxOptions;

    static {
        vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
        vertxOptions.getEventBusOptions().setClustered(true);
    }

    @Test
    public void testVertxModular() {
        var injector = Guice.createInjector(new VertxModular(vertxOptions).createModule());
        var vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        var reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        var eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof ClusteredEventBus);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    @Test
    public void testVertxModularProviderClass() {
        var injector = Guice.createInjector(new VertxModular(CustomOptionsProvider.class).createModule());
        var vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        var reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        var eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof ClusteredEventBus);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    public static class CustomOptionsProvider implements Provider<VertxOptions> {

        @Override
        public VertxOptions get() {
            return vertxOptions;
        }
    }
}
