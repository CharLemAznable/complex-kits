package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.Mapp;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.ContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigRequestMethod;
import com.github.charlemaznable.core.net.ohclient.param.OhContext;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedContext;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Map;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.lang.Mapp.newHashMap;
import static com.github.charlemaznable.core.lang.Str.toStr;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class OhContextTest {

    @SneakyThrows
    @Test
    public void testOhContext() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                val body = unJson(request.getBody().readUtf8());
                switch (request.getPath()) {
                    case "/sampleDefault":
                        assertEquals("CV1", body.get("C1"));
                        assertEquals("CV2", body.get("C2"));
                        assertNull(body.get("C3"));
                        assertNull(body.get("C4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleMapping":
                        assertEquals("CV1", body.get("C1"));
                        assertNull(body.get("C2"));
                        assertEquals("CV3", body.get("C3"));
                        assertNull(body.get("C4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleContexts":
                        assertEquals("CV1", body.get("C1"));
                        assertNull(body.get("C2"));
                        assertNull(body.get("C3"));
                        assertEquals("CV4", body.get("C4"));
                        return new MockResponse().setBody("OK");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41170);

        val httpClient = getClient(ContextHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleContexts(null, "V4"));

        mockWebServer.shutdown();
    }

    @OhConfigRequestMethod(RequestMethod.POST)
    @OhConfigContentFormat(TestContextFormat.class)
    @OhFixedContext(name = "C1", value = "V1")
    @OhFixedContext(name = "C2", valueProvider = C2Provider.class)
    @OhClient("${root}:41170")
    public interface ContextHttpClient {

        String sampleDefault();

        @OhFixedContext(name = "C2", valueProvider = C2Provider.class)
        @OhFixedContext(name = "C3", value = "V3")
        String sampleMapping();

        @OhFixedContext(name = "C2", valueProvider = C2Provider.class)
        @OhFixedContext(name = "C3", value = "V3")
        String sampleContexts(@OhContext("C3") String v3,
                              @OhContext("C4") String v4);
    }

    public static class C2Provider implements OhFixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return null;
        }
    }

    public static class TestContextFormat implements ContentFormat {

        private Map<String, String> contextValue = Mapp.of(
                "V1", "CV1",
                "V2", "CV2",
                "V3", "CV3",
                "V4", "CV4"
        );

        @Override
        public String contentType() {
            return APPLICATION_JSON_VALUE;
        }

        @Override
        public String format(@Nonnull Map<String, Object> parameterMap,
                             @Nonnull Map<String, Object> contextMap) {
            Map<String, String> content = newHashMap();
            for (val contextEntry : contextMap.entrySet()) {
                content.put(contextEntry.getKey(), contextValue
                        .get(toStr(contextEntry.getValue())));
            }
            return json(content);
        }
    }
}
