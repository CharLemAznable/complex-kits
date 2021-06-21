package com.github.charlemaznable.core.net.vxclient;

import com.github.charlemaznable.core.net.common.CommonReqTest;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.JsonContentFormatter;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.StatusError;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class VxReqCommonTest extends CommonReqTest {

    public void testVxReq(Vertx vertx, VertxTestContext test) {
        startMockWebServer(9300);

        CompositeFuture.all(newArrayList(
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample1")
                                .acceptCharset(ISO_8859_1)
                                .contentFormat(new FormContentFormatter())
                                .header("AAA", "aaa")
                                .headers(of("AAA", null, "BBB", "bbb"))
                                .parameter("CCC", "ccc")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample1", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx).req("http://127.0.0.1:9300/sample2")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample2", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300")
                                .req("/sample3?DDD=ddd")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample3", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300")
                                .req("/sample4")
                                .parameter("AAA", "aaa")
                                .parameters(of("AAA", null, "BBB", "bbb"))
                                .requestBody("CCC=ccc")
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample4", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample5")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample5")
                                .parameter("AAA", "aaa")
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof StatusError);
                                    StatusError e = (StatusError) async.cause();
                                    assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample6")
                                .statusErrorMapping(HttpStatus.NOT_FOUND, NotFoundException.class)
                                .statusSeriesErrorMapping(HttpStatus.Series.CLIENT_ERROR, ClientErrorException.class)
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof NotFoundException);
                                    NotFoundException e = (NotFoundException) async.cause();
                                    assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample6")
                                .parameter("AAA", "aaa")
                                .statusErrorMapping(HttpStatus.NOT_FOUND, NotFoundException.class)
                                .statusSeriesErrorMapping(HttpStatus.Series.CLIENT_ERROR, ClientErrorException.class)
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof ClientErrorException);
                                    ClientErrorException e = (ClientErrorException) async.cause();
                                    assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
                                    assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
                                    f.complete();
                                }))),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample7")
                                .contentFormat(new JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .get(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9300/sample7")
                                .contentFormat(new JsonContentFormatter())
                                .parameter("BBB", "bbb")
                                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                                .post(async -> test.verify(() ->
                                        assertEquals("Sample7", async.result())), f)),
                Future.<String>future(f ->
                        new VxReq(vertx, "http://127.0.0.1:9399/error")
                                .proxyOptions(null)
                                .keyCertOptions(null)
                                .trustOptions(null)
                                .verifyHost(true)
                                .connectTimeout(1000)
                                .get(async -> test.verify(() -> {
                                    assertTrue(async.cause() instanceof VxException);
                                    f.complete();
                                }), null))
        )).onComplete(result -> {
            shutdownMockWebServer();
            test.<CompositeFuture>succeedingThenComplete().handle(result);
        });
    }

    public static class NotFoundException extends StatusError {

        private static final long serialVersionUID = -7904971326378842916L;

        public NotFoundException(int statusCode, String message) {
            super(statusCode, message);
        }
    }

    public static class ClientErrorException extends StatusError {

        private static final long serialVersionUID = -1930086236315973535L;

        public ClientErrorException(int statusCode, String message) {
            super(statusCode, message);
        }
    }
}
