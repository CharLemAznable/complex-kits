package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.DefaultErrorMappingDisabled;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.Mapping.UrlProvider;
import com.github.charlemaznable.core.net.common.ProviderException;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UrlConcatTest {

    private static final String ROOT = "Root";
    private static final String SAMPLE = "Sample";
    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testUrlPlainConcat() {
        try (val mockWebServer = startMockWebServer(41100)) {

            val httpClient = ohLoader.getClient(UrlPlainHttpClient.class);
            assertEquals(ROOT, httpClient.empty());
            assertEquals(ROOT, httpClient.root());
            assertEquals(SAMPLE, httpClient.sample());
            assertEquals(SAMPLE, httpClient.sampleWithSlash());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound());
        }
    }

    @SneakyThrows
    @Test
    public void testUrlProviderConcat() {
        try (val mockWebServer = startMockWebServer(41101)) {

            val httpClient = ohLoader.getClient(UrlProviderHttpClient.class);
            assertEquals(ROOT, httpClient.empty());
            assertEquals(ROOT, httpClient.root());
            assertEquals(SAMPLE, httpClient.sample());
            assertEquals(SAMPLE, httpClient.sampleWithSlash());
            assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound());
        }
    }

    @SneakyThrows
    @Test
    public void testErrorUrl() {
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorUrlHttpClient1.class));

        val httpClient = ohLoader.getClient(ErrorUrlHttpClient2.class);
        assertThrows(ProviderException.class, httpClient::sample);
    }

    @SneakyThrows
    private MockWebServer startMockWebServer(int port) {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/":
                        return new MockResponse().setBody(ROOT);
                    case "/sample":
                        return new MockResponse().setBody(SAMPLE);
                    default:
                        return new MockResponse()
                                .setResponseCode(HttpStatus.NOT_FOUND.value())
                                .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            }
        });
        mockWebServer.start(port);
        return mockWebServer;
    }

    @DefaultErrorMappingDisabled
    @OhClient
    @Mapping("${root}:41100")
    public interface UrlPlainHttpClient {

        @Mapping
        String empty();

        @Mapping("/")
        String root();

        String sample();

        @Mapping(urlProvider = TestUrlProvider.class)
        String sampleWithSlash();

        String notFound();
    }

    @DefaultErrorMappingDisabled
    @OhClient
    @Mapping(urlProvider = TestUrlProvider.class)
    public interface UrlProviderHttpClient {

        @Mapping
        String empty();

        @Mapping("/")
        String root();

        String sample();

        @Mapping(urlProvider = TestUrlProvider.class)
        String sampleWithSlash();

        String notFound();
    }

    @OhClient
    @Mapping(urlProvider = ClassErrorUrlProvider.class)
    public interface ErrorUrlHttpClient1 {}

    @OhClient
    @Mapping(urlProvider = MethodErrorUrlProvider.class)
    public interface ErrorUrlHttpClient2 {

        @Mapping(urlProvider = MethodErrorUrlProvider.class)
        String sample();
    }

    public static class TestUrlProvider implements UrlProvider {

        @Override
        public String url(Class<?> clazz) {
            return "http://127.0.0.1:41101";
        }

        @Override
        public String url(Class<?> clazz, Method method) {
            return "/sample";
        }
    }

    public static class ClassErrorUrlProvider implements UrlProvider {}

    public static class MethodErrorUrlProvider implements UrlProvider {

        @Override
        public String url(Class<?> clazz) {
            return "http://127.0.0.1:41101/";
        }
    }
}
