package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.param.OhFixedPathVar;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import com.github.charlemaznable.core.net.ohclient.param.OhPathVar;
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

public class OhPathVarTest {

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
    }

    @OhFixedPathVar(name = "P1", value = "V1")
    @OhFixedPathVar(name = "P2", valueProvider = P2Provider.class)
    @OhClient("${root}:41150/{P1}/{P2}")
    public interface PathVarHttpClient {

        @OhMapping("")
        String sampleDefault();

        @OhFixedPathVar(name = "P2", valueProvider = P2Provider.class)
        @OhMapping("")
        String sampleMapping();

        @OhFixedPathVar(name = "P2", value = "V3")
        @OhMapping("")
        String samplePathVars(@OhPathVar("P2") String v4);
    }

    public static class P2Provider implements OhFixedValueProvider {

        @Override
        public String value(Class<?> clazz) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method) {
            return "V3";
        }
    }
}
