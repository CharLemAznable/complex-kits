package com.github.charlemaznable.core.vertx.spring;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.vertx.diamond.VertxElf.buildVertx;

@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
public class SpringVertxConfiguration {

    private final VertxOptions vertxOptions;

    @Autowired
    public SpringVertxConfiguration(@Nullable VertxOptions vertxOptions) {
        this.vertxOptions = nullThen(vertxOptions, VertxOptions::new);
    }

    @Bean
    public Vertx vertx() {
        return buildVertx(vertxOptions);
    }
}
