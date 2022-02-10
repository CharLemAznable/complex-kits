package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.DefaultFallbackDisabled;
import com.github.charlemaznable.core.net.common.FallbackFunction;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.StatusError;
import com.github.charlemaznable.core.net.common.StatusFallback;
import com.github.charlemaznable.core.net.common.StatusSeriesFallback;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhResponseMappingTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhResponseMapping() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleNotFound":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                        case "/sampleClientError":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.FORBIDDEN.value())
                                    .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                        case "/sampleMappingNotFound":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                        case "/sampleMappingClientError":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.FORBIDDEN.value())
                                    .setBody(HttpStatus.FORBIDDEN.getReasonPhrase());
                        case "/sampleServerError":
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .setBody(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                        default:
                            return new MockResponse().setBody("OK");
                    }
                }
            });
            mockWebServer.start(41180);

            val httpClient = ohLoader.getClient(MappingHttpClient.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), httpClient.sampleClientError());
            assertEquals("\"" + HttpStatus.NOT_FOUND.getReasonPhrase() + "\"", httpClient.sampleMappingNotFound());
            assertEquals("\"" + HttpStatus.FORBIDDEN.getReasonPhrase() + "\"", httpClient.sampleMappingClientError());
            assertThrows(StatusError.class, httpClient::sampleServerError);

            val defaultHttpClient = ohLoader.getClient(DefaultMappingHttpClient.class);
            try {
                defaultHttpClient.sampleNotFound();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleClientError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleMappingNotFound();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.NOT_FOUND.value(), er.getStatusCode());
                assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleMappingClientError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.FORBIDDEN.value(), er.getStatusCode());
                assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), er.getMessage());
            }
            try {
                defaultHttpClient.sampleServerError();
            } catch (Exception e) {
                assertEquals(StatusError.class, e.getClass());
                StatusError er = (StatusError) e;
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), er.getStatusCode());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), er.getMessage());
            }

            val disabledHttpClient = ohLoader.getClient(DisabledMappingHttpClient.class);
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleClientError());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), disabledHttpClient.sampleMappingNotFound());
            assertEquals(HttpStatus.FORBIDDEN.getReasonPhrase(), disabledHttpClient.sampleMappingClientError());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), disabledHttpClient.sampleServerError());
        }
    }

    @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound.class)
    @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError.class)
    @Mapping("${root}:41180")
    @OhClient
    public interface MappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        String sampleMappingNotFound();

        @StatusFallback(status = HttpStatus.NOT_FOUND, fallback = NotFound2.class)
        @StatusSeriesFallback(statusSeries = HttpStatus.Series.CLIENT_ERROR, fallback = ClientError2.class)
        String sampleMappingClientError();

        String sampleServerError();
    }

    @Mapping("${root}:41180")
    @OhClient
    public interface DefaultMappingHttpClient {

        void sampleNotFound();

        void sampleClientError();

        void sampleMappingNotFound();

        void sampleMappingClientError();

        void sampleServerError();
    }

    @DefaultFallbackDisabled
    @Mapping("${root}:41180")
    @OhClient
    public interface DisabledMappingHttpClient {

        String sampleNotFound();

        String sampleClientError();

        String sampleMappingNotFound();

        String sampleMappingClientError();

        String sampleServerError();
    }

    public static class NotFound implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }

    public static class ClientError implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return response.responseBodyAsString();
        }
    }

    public static class NotFound2 implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return "\"" + response.responseBodyAsString() + "\"";
        }
    }

    public static class ClientError2 implements FallbackFunction<String> {

        @Override
        public String apply(Response response) {
            return "\"" + response.responseBodyAsString() + "\"";
        }
    }
}
