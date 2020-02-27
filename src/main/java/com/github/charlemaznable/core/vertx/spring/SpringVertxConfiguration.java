package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
public class SpringVertxConfiguration {

    @Bean
    public Vertx vertx(@Nullable VertxOptions vertxOptions) {
        return Vertx.vertx(nullThen(vertxOptions, VertxOptions::new));
    }
}
