package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTripleTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testTriple() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleStatusCodeAndBean":
                        case "/sampleFutureStatusCodeAndBean":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(new Bean("John")));
                        case "/sampleRawStreamAndBean":
                        case "/sampleFutureRawStreamAndBean":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(new Bean("Doe")));
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41195);
            val httpClient = ohLoader.getClient(TripleHttpClient.class);

            Triple<Integer, HttpStatus, Bean> triple = httpClient.sampleStatusCodeAndBean();
            assertEquals(HttpStatus.OK.value(), triple.getLeft());
            assertEquals(HttpStatus.OK, triple.getMiddle());
            assertEquals("John", triple.getRight().getName());
            val futureTriple = httpClient.sampleFutureStatusCodeAndBean();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureTriple::isDone);
            triple = futureTriple.get();
            assertEquals(HttpStatus.OK.value(), triple.getLeft());
            assertEquals(HttpStatus.OK, triple.getMiddle());
            assertEquals("John", triple.getRight().getName());

            Triple<InputStream, String, Bean> rawTriple = httpClient.sampleRawStreamAndBean();
            @Cleanup val isr1 = new InputStreamReader(rawTriple.getLeft(), "UTF-8");
            try (val bufferedReader = new BufferedReader(isr1)) {
                assertEquals(json(new Bean("Doe")), bufferedReader.readLine());
            }
            assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
            assertEquals("Doe", rawTriple.getRight().getName());
            val futureRawTriple = httpClient.sampleFutureRawStreamAndBean();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureRawTriple::isDone);
            rawTriple = futureRawTriple.get();
            @Cleanup val isr2 = new InputStreamReader(rawTriple.getLeft(), "UTF-8");
            try (val bufferedReader = new BufferedReader(isr2)) {
                assertEquals(json(new Bean("Doe")), bufferedReader.readLine());
            }
            assertEquals(json(new Bean("Doe")), rawTriple.getMiddle());
            assertEquals("Doe", rawTriple.getRight().getName());
        }
    }

    @OhClient
    @Mapping("${root}:41195")
    public interface TripleHttpClient {

        Triple<Integer, HttpStatus, Bean> sampleStatusCodeAndBean();

        Future<Triple<Integer, HttpStatus, Bean>> sampleFutureStatusCodeAndBean();

        Triple<InputStream, String, Bean> sampleRawStreamAndBean();

        Future<Triple<InputStream, String, Bean>> sampleFutureRawStreamAndBean();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
