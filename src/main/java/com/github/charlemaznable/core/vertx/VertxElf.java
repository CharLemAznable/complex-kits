package com.github.charlemaznable.core.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

public final class VertxElf {

    private VertxElf() {
        throw new UnsupportedOperationException();
    }

    @SneakyThrows
    public static Vertx buildVertx(VertxOptions vertxOptions) {
        if (vertxOptions.getEventBusOptions().isClustered()) {
            var completableFuture = new CompletableFuture<Vertx>();
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
