package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhDefaultErrorMappingDisabled;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReturnTest {

    @SneakyThrows
    @Test
    public void testStatusCode() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sampleVoid":
                    case "/sampleFutureVoid":
                        return new MockResponse().setResponseCode(HttpStatus.OK.value());
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
        val httpClient = getClient(StatusCodeHttpClient.class);

        assertDoesNotThrow(httpClient::sampleVoid);
        val futureVoid = httpClient.sampleFutureVoid();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(futureVoid::isDone);
        assertDoesNotThrow((ThrowingSupplier<Void>) futureVoid::get);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClient.sampleStatusCode());
        val futureStatusCode = httpClient.sampleFutureStatusCode();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(futureStatusCode::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), futureStatusCode.get());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpClient.sampleStatus());
        val futureStatus = httpClient.sampleFutureStatus();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(futureStatus::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, futureStatus.get());

        assertEquals(HttpStatus.Series.SERVER_ERROR, httpClient.sampleStatusSeries());
        val futureStatusSeries = httpClient.sampleFutureStatusSeries();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(futureStatusSeries::isDone);
        assertEquals(HttpStatus.Series.SERVER_ERROR, futureStatusSeries.get());

        assertTrue(httpClient.sampleSuccess());
        val futureFailure = httpClient.sampleFailure();
        await().forever().pollDelay(Duration.ofMillis(1000)).until(futureFailure::isDone);
        assertFalse(futureFailure.get());

        mockWebServer.shutdown();
    }

    @OhDefaultErrorMappingDisabled
    @OhClient("${root}:41190")
    public interface StatusCodeHttpClient {

        void sampleVoid();

        Future<Void> sampleFutureVoid();

        int sampleStatusCode();

        Future<Integer> sampleFutureStatusCode();

        @OhMapping("sampleStatusCode")
        HttpStatus sampleStatus();

        @OhMapping("sampleFutureStatusCode")
        Future<HttpStatus> sampleFutureStatus();

        @OhMapping("sampleStatusCode")
        HttpStatus.Series sampleStatusSeries();

        @OhMapping("sampleFutureStatusCode")
        Future<HttpStatus.Series> sampleFutureStatusSeries();

        @OhMapping("sampleVoid")
        boolean sampleSuccess();

        @OhMapping("sampleStatusCode")
        Future<Boolean> sampleFailure();
    }
}
