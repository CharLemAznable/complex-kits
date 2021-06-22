package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.lang.EverythingIsNonNull;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.ProviderException;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientTimeout;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientTimeout.TimeoutProvider;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TimeoutTest {

    private static final String SAMPLE = "Sample";
    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @EverythingIsNonNull
    @SneakyThrows
    @Test
    public void testTimeout() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (checkNotNull(request.getPath())) {
                        case "/sample":
                            return new MockResponse().setBody(SAMPLE);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41210);

            val client1 = ohLoader.getClient(TimeoutHttpClient1.class);
            assertEquals(SAMPLE, client1.sample());

            val client2 = ohLoader.getClient(TimeoutHttpClient2.class);
            assertEquals(SAMPLE, client2.sample());

            val provider1 = ohLoader.getClient(TimeoutProviderHttpClient1.class);
            assertEquals(SAMPLE, provider1.sample());

            val provider2 = ohLoader.getClient(TimeoutProviderHttpClient2.class);
            assertEquals(SAMPLE, provider2.sample());

            val timeout1 = OhFactory.timeout();
            val param1 = ohLoader.getClient(TimeoutParamHttpClient1.class);
            assertEquals(SAMPLE, param1.sample(timeout1));

            val timeout2 = OhFactory.timeout(60_000, 60_000, 60_000, 60_000);
            val param2 = ohLoader.getClient(TimeoutParamHttpClient2.class);
            assertEquals(SAMPLE, param2.sample(timeout2));

            assertEquals(TimeoutProvider.class, timeout1.callTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout1.connectTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout1.readTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout1.writeTimeoutProvider());
            assertEquals(ClientTimeout.class, timeout1.annotationType());
            assertEquals(TimeoutProvider.class, timeout2.callTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout2.connectTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout2.readTimeoutProvider());
            assertEquals(TimeoutProvider.class, timeout2.writeTimeoutProvider());
            assertEquals(ClientTimeout.class, timeout2.annotationType());
        }
    }

    @Test
    public void testTimeoutError() {
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(TimeoutErrorHttpClient1.class));

        val httpClient = ohLoader.getClient(TimeoutErrorHttpClient2.class);
        assertThrows(ProviderException.class, httpClient::sample);
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutHttpClient1 {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutHttpClient2 {

        @ClientTimeout
        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout(
            callTimeoutProvider = Timeout60.class,
            connectTimeoutProvider = Timeout60.class,
            readTimeoutProvider = Timeout60.class,
            writeTimeoutProvider = Timeout60.class
    )
    public interface TimeoutProviderHttpClient1 {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout(
            callTimeoutProvider = Timeout60.class,
            connectTimeoutProvider = Timeout60.class,
            readTimeoutProvider = Timeout60.class,
            writeTimeoutProvider = Timeout60.class
    )
    public interface TimeoutProviderHttpClient2 {

        @ClientTimeout(
                callTimeoutProvider = Timeout60.class,
                connectTimeoutProvider = Timeout60.class,
                readTimeoutProvider = Timeout60.class,
                writeTimeoutProvider = Timeout60.class
        )
        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutParamHttpClient1 {

        String sample(ClientTimeout clientTimeout);
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout
    public interface TimeoutParamHttpClient2 {

        @ClientTimeout
        String sample(ClientTimeout clientTimeout);
    }

    @OhClient
    @Mapping("${root}:41210")
    @ClientTimeout(connectTimeoutProvider = TimeoutError1.class)
    public interface TimeoutErrorHttpClient1 {

        String sample();
    }

    @OhClient
    @Mapping("${root}:41210")
    public interface TimeoutErrorHttpClient2 {

        @ClientTimeout(connectTimeoutProvider = TimeoutError2.class)
        String sample();
    }

    public static class Timeout60 implements TimeoutProvider {

        @Override
        public long timeout(Class<?> clazz) {
            return 60_000;
        }

        @Override
        public long timeout(Class<?> clazz, Method method) {
            return 60_000;
        }
    }

    public static class TimeoutError1 implements TimeoutProvider {}

    public static class TimeoutError2 implements TimeoutProvider {

        @Override
        public long timeout(Class<?> clazz) {
            return 60_000;
        }
    }
}
