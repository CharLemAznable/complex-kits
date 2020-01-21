package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhDefaultErrorMappingDisabled;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnTest {

    @SneakyThrows
    @Test
    public void testStatusCode() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sampleStatusCode":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                    case "/sampleFutureStatusCode":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_IMPLEMENTED.value())
                                .setBody(HttpStatus.NOT_IMPLEMENTED.getReasonPhrase());
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41190);

        val httpClient = getClient(ReturnHttpClient.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClient.sampleStatusCode());
        val future = httpClient.sampleFutureStatusCode();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(future::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), future.get());

        mockWebServer.shutdown();
    }

    @OhDefaultErrorMappingDisabled
    @OhClient("${root}:41190")
    public interface ReturnHttpClient {

        int sampleStatusCode();

        Future<Integer> sampleFutureStatusCode();
    }
}
