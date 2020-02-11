package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.FixedPathVar;
import com.github.charlemaznable.core.net.common.FixedValueProvider;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.PathVar;
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

public class PathVarTest {

    @SneakyThrows
    @Test
    public void testOhPathVar() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/V1/V2":
                        return new MockResponse().setBody("V2");
                    case "/V1/V3":
                        return new MockResponse().setBody("V3");
                    case "/V1/V4":
                        return new MockResponse().setBody("V4");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41150);

        val httpClient = getClient(PathVarHttpClient.class);
        assertEquals("V2", httpClient.sampleDefault());
        assertEquals("V3", httpClient.sampleMapping());
        assertEquals("V4", httpClient.samplePathVars("V4"));

        mockWebServer.shutdown();
    }

    @FixedPathVar(name = "P1", value = "V1")
    @FixedPathVar(name = "P2", valueProvider = P2Provider.class)
    @Mapping("${root}:41150/{P1}/{P2}")
    @OhClient
    public interface PathVarHttpClient {

        @Mapping
        String sampleDefault();

        @FixedPathVar(name = "P2", valueProvider = P2Provider.class)
        @Mapping
        String sampleMapping();

        @FixedPathVar(name = "P2", value = "V3")
        @Mapping
        String samplePathVars(@PathVar("P2") String v4);
    }

    public static class P2Provider implements FixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return "V3";
        }
    }
}
