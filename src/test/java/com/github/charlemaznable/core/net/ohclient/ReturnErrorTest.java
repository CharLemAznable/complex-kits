package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.codec.Json.jsonOf;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReturnErrorTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testError() {
        try (var mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleFuture":
                        case "/sampleList":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(newArrayList("John", "Doe")));
                        case "/sampleMapError":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody("John Doe");
                        case "/sampleMap":
                        case "/samplePair":
                        case "/sampleTriple":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(jsonOf("John", "Doe"));
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41196);
            var httpClient = ohLoader.getClient(ErrorHttpClient.class);

            assertThrows(OhException.class, httpClient::sampleFuture);
            assertThrows(OhException.class, httpClient::sampleList);
            assertThrows(IllegalArgumentException.class, httpClient::sampleMapError);

            var map = httpClient.sampleMap();
            assertEquals("Doe", map.get("John"));

            assertThrows(OhException.class, httpClient::samplePair);
            assertThrows(OhException.class, httpClient::sampleTriple);
        }
    }

    @OhClient
    @Mapping("${root}:41196")
    public interface ErrorHttpClient {

        Future sampleFuture();

        List sampleList();

        Map sampleMapError();

        Map sampleMap();

        Pair samplePair();

        Triple sampleTriple();
    }
}
