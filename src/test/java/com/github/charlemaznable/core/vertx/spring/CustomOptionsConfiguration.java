package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.VertxOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringVertxImport
public class CustomOptionsConfiguration {

    static final int DEFAULT_WORKER_POOL_SIZE = 42;

    @Bean
    public VertxOptions vertxOptions() {
        return new VertxOptions().setWorkerPoolSize(DEFAULT_WORKER_POOL_SIZE);
    }
}