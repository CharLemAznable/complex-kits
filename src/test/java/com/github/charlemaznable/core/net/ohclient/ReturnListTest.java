package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Future;

import static com.github.charlemaznable.core.codec.Json.json;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReturnListTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testList() {
        try (var mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleListBean":
                        case "/sampleFutureListBean":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(newArrayList(new Bean("John"), new Bean("Doe"))));
                        case "/sampleListString":
                        case "/sampleFutureListString":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(json(newArrayList("John", "Doe")));
                        case "/sampleListBufferedSource":
                            return new MockResponse().setResponseCode(HttpStatus.OK.value())
                                    .setBody(HttpStatus.OK.getReasonPhrase());
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41192);
            var httpClient = ohLoader.getClient(ListHttpClient.class);

            var beans = httpClient.sampleListBean();
            var bean1 = beans.get(0);
            var bean2 = beans.get(1);
            assertEquals("John", bean1.getName());
            assertEquals("Doe", bean2.getName());
            var futureBeans = httpClient.sampleFutureListBean();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureBeans::isDone);
            beans = futureBeans.get();
            bean1 = beans.get(0);
            bean2 = beans.get(1);
            assertEquals("John", bean1.getName());
            assertEquals("Doe", bean2.getName());

            var strs = httpClient.sampleListString();
            var str1 = strs.get(0);
            var str2 = strs.get(1);
            assertEquals("John", str1);
            assertEquals("Doe", str2);
            var futureStrs = httpClient.sampleFutureListString();
            await().forever().pollDelay(Duration.ofMillis(100)).until(futureStrs::isDone);
            strs = futureStrs.get();
            str1 = strs.get(0);
            str2 = strs.get(1);
            assertEquals("John", str1);
            assertEquals("Doe", str2);

            var bufferedSources = httpClient.sampleListBufferedSource();
            assertEquals(1, bufferedSources.size());
            assertEquals(HttpStatus.OK.getReasonPhrase(), bufferedSources.get(0).readUtf8());
        }
    }

    @OhClient
    @Mapping("${root}:41192")
    public interface ListHttpClient {

        List<Bean> sampleListBean();

        Future<List<Bean>> sampleFutureListBean();

        List<String> sampleListString();

        Future<List<String>> sampleFutureListString();

        List<BufferedSource> sampleListBufferedSource();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bean {

        private String name;
    }
}
