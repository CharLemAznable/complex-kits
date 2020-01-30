package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedHeader;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import com.github.charlemaznable.core.net.ohclient.param.OhHeader;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhHeaderTest {

    @SneakyThrows
    @Test
    public void testOhHeader() {
        val mockWebServer = new MockWebServer();
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

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testErrorOhHeader() {
        assertThrows(OhException.class, () ->
                getClient(ErrorFixedHttpClient1.class));

        val httpClient = getClient(ErrorFixedHttpClient2.class);
        assertThrows(OhException.class, httpClient::sample);
    }

    @OhFixedHeader(name = "H1", value = "V1")
    @OhFixedHeader(name = "H2", valueProvider = H2Provider.class)
    @OhClient("${root}:41140")
    public interface HeaderHttpClient {

        String sampleDefault();

        @OhFixedHeader(name = "H2", valueProvider = H2Provider.class)
        @OhFixedHeader(name = "H3", value = "V3")
        String sampleMapping();

        @OhFixedHeader(name = "H2", valueProvider = H2Provider.class)
        @OhFixedHeader(name = "H3", value = "V3")
        String sampleHeaders(@OhHeader("H3") String v3,
                             @OhHeader("H4") String v4);
    }

    @OhFixedHeader(name = "H2", valueProvider = ErrorClassProvider.class)
    @OhClient("${root}:41141")
    public interface ErrorFixedHttpClient1 {}

    @OhFixedHeader(name = "H2", valueProvider = H2Provider.class)
    @OhClient("${root}:41142")
    public interface ErrorFixedHttpClient2 {

        @OhFixedHeader(name = "H2", valueProvider = ErrorMethodProvider.class)
        String sample();
    }

    public static class H2Provider implements OhFixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return null;
        }
    }

    public static class ErrorClassProvider implements OhFixedValueProvider {}

    public static class ErrorMethodProvider implements OhFixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }
    }
}
