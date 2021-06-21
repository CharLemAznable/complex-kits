package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Guice;
import com.google.inject.Provider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomOptionsTest {

    static final int DEFAULT_WORKER_POOL_SIZE = 42;

    @Test
    public void testVertxModular() {
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
        vertxOptions.setClusterManager(new HazelcastClusterManager());
        val injector = Guice.createInjector(new VertxModular(vertxOptions).createModule());
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        val reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        val eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof ClusteredEventBus);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    @Test
    public void testVertxModularProviderClass() {
        val injector = Guice.createInjector(new VertxModular(CustomOptionsProvider.class).createModule());
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        val reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        val eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof ClusteredEventBus);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    public static class CustomOptionsProvider implements Provider<VertxOptions> {

        @Override
        public VertxOptions get() {
            val vertxOptions = new VertxOptions();
            vertxOptions.setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
            vertxOptions.setClusterManager(new HazelcastClusterManager());
            return vertxOptions;
        }
    }
}
