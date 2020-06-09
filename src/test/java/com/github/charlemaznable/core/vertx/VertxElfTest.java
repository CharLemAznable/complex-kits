package com.github.charlemaznable.core.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class VertxElfTest {

    @Test
    public void testVertxElf(Vertx vertx, VertxTestContext testContext) {
        assertThrows(ReflectException.class, () ->
                onClass(VertxElf.class).create().get());

        vertx.deployVerticle(new TestVerticle(),
                testContext.succeeding(id -> testContext.completeNow()));
    }

    public static class TestVerticle extends AbstractVerticle {

        @Override
        public void start(Promise<Void> startPromise) {
            CompositeFuture.all(newArrayList(
                    Future.<Void>future(f ->
                            VertxElf.<Void>executeBlocking(promise -> {
                                throw new UnsupportedOperationException();
                            }, asyncResult -> {
                                assertTrue(asyncResult.failed());
                                assertTrue(asyncResult.cause() instanceof UnsupportedOperationException);
                                f.complete();
                            })
                    ),
                    Future.<Void>future(f ->
                            VertxElf.<Void>executeBlocking(Promise::complete,
                                    asyncResult -> {
                                        assertTrue(asyncResult.succeeded());
                                        assertNull(asyncResult.result());
                                        f.complete();
                                    })
                    )
            )).onComplete(asyncResult -> {
                if (asyncResult.failed()) {
                    startPromise.fail(asyncResult.cause());
                } else {
                    startPromise.complete();
                }
            });
        }
    }
}
