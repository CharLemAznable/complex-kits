package com.github.charlemaznable.core.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.SneakyThrows;
import lombok.val;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.nonNull;

public final class VertxElf {

    private static final String CLUSTER_MANAGER_CLASS_PROPERTY = "vertx.cluster.managerClass";

    private VertxElf() {
        throw new UnsupportedOperationException();
    }

    @SneakyThrows
    public static Vertx buildVertx(VertxOptions vertxOptions) {
        if (nonNull(vertxOptions.getClusterManager()) ||
                nonNull(System.getProperty(CLUSTER_MANAGER_CLASS_PROPERTY))) {
            val completableFuture = new CompletableFuture<Vertx>();
            Vertx.clusteredVertx(vertxOptions, asyncResult -> {
                if (asyncResult.failed()) {
                    completableFuture.completeExceptionally(asyncResult.cause());
                } else {
                    completableFuture.complete(asyncResult.result());
                }
            });
            return completableFuture.get();
        } else {
            return Vertx.vertx(vertxOptions);
        }
    }

    public static <V> void executeBlocking(
            Handler<Promise<V>> blockingCodeHandler,
            Handler<AsyncResult<V>> resultHandler) {
        Vertx.currentContext().executeBlocking(block -> {
            try {
                blockingCodeHandler.handle(block);
            } catch (Exception e) {
                block.fail(e);
            }
        }, false, resultHandler);
    }
}
