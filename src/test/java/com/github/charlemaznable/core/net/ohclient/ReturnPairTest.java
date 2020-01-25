package com.github.charlemaznable.core.net.ohclient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnPairTest {

    @SneakyThrows
    @Test
    public void testPair() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sampleStatusAndBean":
                    case "/sampleFutureStatusAndBean":
                        return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                .setBody(json(new Bean("John")));
                    case "/sampleRawAndBean":
                    case "/sampleFutureRawAndBean":
                        return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                .setBody(json(new Bean("Doe")));
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41194);
        val httpClient = getClient(PairHttpClient.class);

        var pair = httpClient.sampleStatusAndBean();
        assertEquals(HttpStatus.OK.value(), pair.getKey());
        assertEquals("John", pair.getValue().getName());
        val futurePair = httpClient.sampleFutureStatusAndBean();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futurePair::isDone);
        pair = futurePair.get();
        assertEquals(HttpStatus.OK.value(), pair.getKey());
        assertEquals("John", pair.getValue().getName());

        var rawPair = httpClient.sampleRawAndBean();
        assertEquals(json(new Bean("Doe")), rawPair.getKey());
        assertEquals("Doe", rawPair.getValue().getName());
        val futureRawPair = httpClient.sampleFutureRawAndBean();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureRawPair::isDone);
        rawPair = futureRawPair.get();
        assertEquals(json(new Bean("Doe")), rawPair.getKey());
        assertEquals("Doe", rawPair.getValue().getName());

        mockWebServer.shutdown();
    }

    @OhClient("${root}:41194")
    public interface PairHttpClient {

        Pair<Integer, Bean> sampleStatusAndBean();

        Future<Pair<Integer, Bean>> sampleFutureStatusAndBean();

        Pair<String, Bean> sampleRawAndBean();

        Future<Pair<String, Bean>> sampleFutureRawAndBean();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
