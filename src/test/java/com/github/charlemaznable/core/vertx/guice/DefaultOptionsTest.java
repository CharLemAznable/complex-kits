package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Guice;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.impl.EventBusImpl;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultOptionsTest {

    @Test
    public void testVertxModular() {
        var injector = Guice.createInjector(new VertxModular().createModule());
        var vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        var reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(VertxOptions.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        assertSame(vertx, injector.getInstance(Vertx.class));
        var eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof EventBusImpl);
    }
}
