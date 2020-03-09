package com.github.charlemaznable.core.vertx.guice;

import com.google.inject.Guice;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class DefaultOptionsTest {

    @Test
    public void testVertxInjector() {
        val injector = Guice.createInjector(new VertxModular().createModule());
        val vertx = injector.getInstance(Vertx.class);
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(VertxOptions.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        assertSame(vertx, injector.getInstance(Vertx.class));
    }
}
