package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.impl.clustered.ClusteredEventBus;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CustomOptionsConfiguration.class)
public class CustomOptionsTest {

    @Autowired
    private Vertx vertx;

    @Test
    public void testSpringVertxConfiguration() {
        assertNotNull(vertx);
        val reflectVertx = on(vertx);
        int defaultWorkerPoolSize = reflectVertx.field("defaultWorkerPoolSize").get();
        assertEquals(CustomOptionsConfiguration.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
        val eventBus = reflectVertx.field("eventBus").get();
        assertTrue(eventBus instanceof ClusteredEventBus);
    }
}
