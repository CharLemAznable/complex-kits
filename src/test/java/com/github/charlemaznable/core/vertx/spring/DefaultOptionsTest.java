package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.joor.Reflect.on;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefaultOptionsConfiguration.class)
public class DefaultOptionsTest {

    @Autowired
    private Vertx vertx;

    @Test
    public void testSpringVertxConfiguration() {
        assertNotNull(vertx);
        int defaultWorkerPoolSize = on(vertx).field("defaultWorkerPoolSize").get();
        assertEquals(VertxOptions.DEFAULT_WORKER_POOL_SIZE, defaultWorkerPoolSize);
    }
}