package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.VertxOptions;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringVertxImport
public class CustomOptionsConfiguration {

    static final int DEFAULT_WORKER_POOL_SIZE = 42;

    @Bean
    public VertxOptions vertxOptions() {
        val vertxOptions = new VertxOptions();
        vertxOptions.setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
        vertxOptions.getEventBusOptions().setClustered(true);
        return vertxOptions;
    }
}
