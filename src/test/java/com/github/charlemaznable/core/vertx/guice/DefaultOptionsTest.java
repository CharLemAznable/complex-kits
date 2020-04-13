package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Guice;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.impl.EventBusImpl;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultOptionsTest {

    @Test
    public void testVertxModular() {
        val injector = Guice.createInjector(new VertxModular().createModule());
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        val reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(VertxOptions.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        assertSame(vertx, injector.getInstance(Vertx.class));
        val eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof EventBusImpl);
    }
}
