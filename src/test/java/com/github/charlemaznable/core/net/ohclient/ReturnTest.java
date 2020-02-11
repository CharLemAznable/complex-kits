package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.DefaultErrorMappingDisabled;
import com.github.charlemaznable.core.net.common.Mapping;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.ResponseBody;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Bytes.string;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureVoid::isDone);
        assertDoesNotThrow((ThrowingSupplier<Void>) futureVoid::get);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), httpClient.sampleStatusCode());
        val futureStatusCode = httpClient.sampleFutureStatusCode();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatusCode::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED.value(), futureStatusCode.get());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpClient.sampleStatus());
        val futureStatus = httpClient.sampleFutureStatus();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatus::isDone);
        assertEquals(HttpStatus.NOT_IMPLEMENTED, futureStatus.get());

        assertEquals(HttpStatus.Series.SERVER_ERROR, httpClient.sampleStatusSeries());
        val futureStatusSeries = httpClient.sampleFutureStatusSeries();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureStatusSeries::isDone);
        assertEquals(HttpStatus.Series.SERVER_ERROR, futureStatusSeries.get());

        assertTrue(httpClient.sampleSuccess());
        val futureFailure = httpClient.sampleFailure();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureFailure::isDone);
        assertFalse(futureFailure.get());

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testResponseBody() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sample":
                        return new MockResponse()
                                .setResponseCode(HttpStatus.OK.value())
                                .setBody(HttpStatus.OK.getReasonPhrase());
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41191);
        val httpClient = getClient(ResponseBodyHttpClient.class);

        assertNotNull(httpClient.sampleResponseBody());
        val futureResponseBody = httpClient.sampleFutureResponseBody();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureResponseBody::isDone);
        assertNotNull(futureResponseBody.get());

        @Cleanup val isr = new InputStreamReader(httpClient.sampleInputStream(), "UTF-8");
        try (val bufferedReader = new BufferedReader(isr)) {
            assertEquals("OK", bufferedReader.readLine());
        }
        val futureInputStream = httpClient.sampleFutureInputStream();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureInputStream::isDone);
        @Cleanup val isr2 = new InputStreamReader(futureInputStream.get(), "UTF-8");
        try (val bufferedReader = new BufferedReader(isr2)) {
            assertEquals("OK", bufferedReader.readLine());
        }

        assertEquals("OK", httpClient.sampleBufferedSource().readUtf8());
        val futureBufferedSource = httpClient.sampleFutureBufferedSource();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureBufferedSource::isDone);
        assertEquals("OK", futureBufferedSource.get().readUtf8());

        assertEquals("OK", string(httpClient.sampleByteArray()));
        val futureByteArray = httpClient.sampleFutureByteArray();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureByteArray::isDone);
        assertEquals("OK", string(futureByteArray.get()));

        try (val bufferedReader = new BufferedReader(httpClient.sampleReader())) {
            assertEquals("OK", bufferedReader.readLine());
        }
        val futureReader = httpClient.sampleFutureReader();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureReader::isDone);
        try (val bufferedReader = new BufferedReader(futureReader.get())) {
            assertEquals("OK", bufferedReader.readLine());
        }

        assertEquals("OK", httpClient.sampleString());
        val futureString = httpClient.sampleFutureString();
        await().forever().pollDelay(Duration.ofMillis(100)).until(futureString::isDone);
        assertEquals("OK", futureString.get());

        mockWebServer.shutdown();
    }

    @DefaultErrorMappingDisabled
    @OhClient
    @Mapping("${root}:41190")
    public interface StatusCodeHttpClient {

        void sampleVoid();

        Future<Void> sampleFutureVoid();

        int sampleStatusCode();

        Future<Integer> sampleFutureStatusCode();

        @Mapping("sampleStatusCode")
        HttpStatus sampleStatus();

        @Mapping("sampleFutureStatusCode")
        Future<HttpStatus> sampleFutureStatus();

        @Mapping("sampleStatusCode")
        HttpStatus.Series sampleStatusSeries();

        @Mapping("sampleFutureStatusCode")
        Future<HttpStatus.Series> sampleFutureStatusSeries();

        @Mapping("sampleVoid")
        boolean sampleSuccess();

        @Mapping("sampleStatusCode")
        Future<Boolean> sampleFailure();
    }

    @Documented
    @Inherited
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Mapping("${root}:41191/sample")
    public @interface TestMapping {}

    @OhClient
    public interface ResponseBodyHttpClient {

        @TestMapping
        ResponseBody sampleResponseBody();

        @TestMapping
        Future<ResponseBody> sampleFutureResponseBody();

        @TestMapping
        InputStream sampleInputStream();

        @TestMapping
        Future<InputStream> sampleFutureInputStream();

        @TestMapping
        BufferedSource sampleBufferedSource();

        @TestMapping
        Future<BufferedSource> sampleFutureBufferedSource();

        @TestMapping
        byte[] sampleByteArray();

        @TestMapping
        Future<byte[]> sampleFutureByteArray();

        @TestMapping
        Reader sampleReader();

        @TestMapping
        Future<Reader> sampleFutureReader();

        @TestMapping
        String sampleString();

        @TestMapping
        Future<String> sampleFutureString();
    }
}
