package com.github.charlemaznable.core.vertx.guice;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultOptionsTest {

    @Test
    public void testVertxInjector() {
        val injector = new VertxInjector().createInjector();
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(VertxOptions.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
    }
}
