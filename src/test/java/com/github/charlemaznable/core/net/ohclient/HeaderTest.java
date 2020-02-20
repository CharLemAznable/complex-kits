package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.FixedHeader;
import com.github.charlemaznable.core.net.common.FixedValueProvider;
import com.github.charlemaznable.core.net.common.Header;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.ProviderException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HeaderTest {

    @SneakyThrows
    @Test
    public void testOhHeader() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleDefault":
                            assertEquals("V1", request.getHeader("H1"));
                            assertEquals("V2", request.getHeader("H2"));
                            assertNull(request.getHeader("H3"));
                            assertNull(request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                        case "/sampleMapping":
                            assertEquals("V1", request.getHeader("H1"));
                            assertNull(request.getHeader("H2"));
                            assertEquals("V3", request.getHeader("H3"));
                            assertNull(request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                        case "/sampleHeaders":
                            assertEquals("V1", request.getHeader("H1"));
                            assertNull(request.getHeader("H2"));
                            assertNull(request.getHeader("H3"));
                            assertEquals("V4", request.getHeader("H4"));
                            return new MockResponse().setBody("OK");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41140);

            val httpClient = getClient(HeaderHttpClient.class);
            assertEquals("OK", httpClient.sampleDefault());
            assertEquals("OK", httpClient.sampleMapping());
            assertEquals("OK", httpClient.sampleHeaders(null, "V4"));
        }
    }

    @SneakyThrows
    @Test
    public void testErrorOhHeader() {
        assertThrows(ProviderException.class, () ->
                getClient(ErrorFixedHttpClient1.class));

        val httpClient = getClient(ErrorFixedHttpClient2.class);
        assertThrows(ProviderException.class, httpClient::sample);
    }

    @FixedHeader(name = "H1", value = "V1")
    @FixedHeader(name = "H2", valueProvider = H2Provider.class)
    @Mapping("${root}:41140")
    @OhClient
    public interface HeaderHttpClient {

        String sampleDefault();

        @FixedHeader(name = "H2", valueProvider = H2Provider.class)
        @FixedHeader(name = "H3", value = "V3")
        String sampleMapping();

        @FixedHeader(name = "H2", valueProvider = H2Provider.class)
        @FixedHeader(name = "H3", value = "V3")
        String sampleHeaders(@Header("H3") String v3,
                             @Header("H4") String v4);
    }

    @FixedHeader(name = "H2", valueProvider = ErrorClassProvider.class)
    @Mapping("${root}:41141")
    @OhClient
    public interface ErrorFixedHttpClient1 {}

    @FixedHeader(name = "H2", valueProvider = H2Provider.class)
    @Mapping("${root}:41142")
    @OhClient
    public interface ErrorFixedHttpClient2 {

        @FixedHeader(name = "H2", valueProvider = ErrorMethodProvider.class)
        String sample();
    }

    public static class H2Provider implements FixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return null;
        }
    }

    public static class ErrorClassProvider implements FixedValueProvider {}

    public static class ErrorMethodProvider implements FixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }
    }
}
