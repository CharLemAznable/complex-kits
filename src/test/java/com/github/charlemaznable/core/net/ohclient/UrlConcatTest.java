package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.OhClient.UrlProvider;
import com.github.charlemaznable.core.net.ohclient.config.OhDefaultErrorMappingDisabled;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrlConcatTest {

    @SneakyThrows
    @Test
    public void testUrlPlainConcat() {
        startMockWebServer(41100);

        val httpClient = getClient(UrlPlainHttpClient.class);
        assertEquals("Root", httpClient.empty());
        assertEquals("Root", httpClient.root());
        assertEquals("Sample", httpClient.sample());
        assertEquals("Sample", httpClient.sampleWithSlash());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound());
    }

    @SneakyThrows
    @Test
    public void testUrlProviderConcat() {
        startMockWebServer(41101);

        val httpClient = getClient(UrlProviderHttpClient.class);
        assertEquals("Root", httpClient.empty());
        assertEquals("Root", httpClient.root());
        assertEquals("Sample", httpClient.sample());
        assertEquals("Sample", httpClient.sampleWithSlash());
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), httpClient.notFound());
    }

    @SneakyThrows
    private void startMockWebServer(int port) {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/":
                        return new MockResponse().setBody("Root");
                    case "/sample":
                        return new MockResponse().setBody("Sample");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(port);
    }

    @OhDefaultErrorMappingDisabled
    @OhClient("${root}:41100")
    public interface UrlPlainHttpClient {

        @OhMapping("")
        String empty();

        @OhMapping("/")
        String root();

        String sample();

        @OhMapping("/sample")
        String sampleWithSlash();

        String notFound();
    }

    @OhDefaultErrorMappingDisabled
    @OhClient(urlProvider = TestUrlProvider.class)
    public interface UrlProviderHttpClient {

        @OhMapping("")
        String empty();

        @OhMapping("/")
        String root();

        String sample();

        @OhMapping("/sample")
        String sampleWithSlash();

        String notFound();
    }

    public static class TestUrlProvider implements UrlProvider {

        @Override
        public String url(Class<?> clazz) {
            return "http://127.0.0.1:41101/";
        }
    }
}
