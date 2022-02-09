package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.CommonReqTest;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.JsonContentFormatter;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.ClientError;
import com.github.charlemaznable.core.net.ohclient.OhResponseMappingTest.NotFound;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.lang.Mapp.of;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OhReqTest extends CommonReqTest {

    @SneakyThrows
    @Test
    public void testOhReq() {
        startMockWebServer(41103);
        val loggingInterceptor = new TestLoggingInterceptor();

        val ohReq1 = new OhReq("http://127.0.0.1:41103/sample1")
                .acceptCharset(ISO_8859_1)
                .contentFormat(new FormContentFormatter())
                .header("AAA", "aaa")
                .headers(of("AAA", null, "BBB", "bbb"))
                .parameter("CCC", "ccc")
                .addInterceptor(loggingInterceptor)
                .loggingLevel(Level.BASIC);
        assertEquals("Sample1", ohReq1.get());

        val ohReq2 = new OhReq()
                .req("http://127.0.0.1:41103/sample2")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .addInterceptor(loggingInterceptor)
                .loggingLevel(Level.BASIC);
        assertEquals("Sample2", ohReq2.post());

        val ohReq3 = new OhReq("http://127.0.0.1:41103")
                .req("/sample3?DDD=ddd")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc")
                .addInterceptor(loggingInterceptor)
                .loggingLevel(Level.BASIC);
        val future3 = ohReq3.getFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future3::isDone);
        assertEquals("Sample3", future3.get());

        val ohReq4 = new OhReq("http://127.0.0.1:41103")
                .req("/sample4")
                .parameter("AAA", "aaa")
                .parameters(of("AAA", null, "BBB", "bbb"))
                .requestBody("CCC=ccc")
                .addInterceptor(loggingInterceptor)
                .loggingLevel(Level.BASIC);
        val future4 = ohReq4.postFuture();
        await().forever().pollDelay(Duration.ofMillis(100)).until(future4::isDone);
        assertEquals("Sample4", future4.get());

        val ohReq5 = new OhReq("http://127.0.0.1:41103/sample5")
                .addInterceptors(newArrayList(loggingInterceptor))
                .loggingLevel(Level.BASIC);
        try {
            ohReq5.get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.NOT_FOUND.value(), e.getStatusCode());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), e.getMessage());
        }
        try {
            ohReq5.parameter("AAA", "aaa").get();
        } catch (StatusError e) {
            assertEquals(HttpStatus.FORBIDDEN.value(), e.getStatusCode());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage());
        }

        val ohReq6 = new OhReq("http://127.0.0.1:41103/sample6")
                .statusFallback(HttpStatus.NOT_FOUND, NotFound.class)
                .statusSeriesFallback(HttpStatus.Series.CLIENT_ERROR, ClientError.class)
                .addInterceptors(newArrayList(loggingInterceptor))
                .loggingLevel(Level.BASIC);
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), ohReq6.get());
        assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), ohReq6.parameter("AAA", "aaa").get());

        val ohReq7 = new OhReq("http://127.0.0.1:41103/sample7")
                .contentFormat(new JsonContentFormatter())
                .parameter("BBB", "bbb")
                .extraUrlQueryBuilder((parameterMap, contextMap) -> "AAA=aaa")
                .addInterceptor(loggingInterceptor)
                .loggingLevel(Level.BASIC);
        assertEquals("Sample7", ohReq7.get());
        assertEquals("Sample7", ohReq7.post());

        shutdownMockWebServer();
    }

    @EverythingIsNonNull
    @Slf4j
    public static class TestLoggingInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            log.info(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            log.info(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
}
