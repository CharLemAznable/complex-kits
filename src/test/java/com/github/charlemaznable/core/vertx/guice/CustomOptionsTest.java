package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Provider;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class CustomOptionsTest {

    static final int DEFAULT_WORKER_POOL_SIZE = 42;
    static final VertxOptions vertxOptions = new VertxOptions().setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);

    @Test
    public void testVertxInjector() {
        val injector = new VertxInjector(vertxOptions).createInjector();
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    @Test
    public void testVertxInjectorProviderClass() {
        val injector = new VertxInjector(CustomOptionsProvider.class).createInjector();
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }

    public static class CustomOptionsProvider implements Provider<VertxOptions> {

        @Override
        public VertxOptions get() {
            return vertxOptions;
        }
    }
}
