package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.ProviderException;
import com.github.charlemaznable.core.net.common.RequestBodyRaw;
import com.github.charlemaznable.core.net.common.RequestMethod;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptor.InterceptorProvider;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientInterceptorCleanup;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientLoggingLevel;
import com.github.charlemaznable.core.net.ohclient.annotation.ClientLoggingLevel.LoggingLevelProvider;
import com.github.charlemaznable.core.net.ohclient.internal.OhDummy;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.miner.MinerElf.minerAsSubstitutor;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InterceptorTest {

    private static final String BODY = "BODY";
    private static final String CONTENT = "OK";
    private static final String HEADER_NAME = "intercept";
    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo("Env", "ohclient",
                "port=41220");
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @SneakyThrows
    @Test
    public void testInterceptorClient() {
        StringSubstitutor ohMinerSubstitutor =
                onClass(OhDummy.class).field("ohMinerSubstitutor").get();
        ohMinerSubstitutor.setVariableResolver(
                minerAsSubstitutor("Env", "ohclient").getStringLookup());
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample1":
                            val values1 = request.getHeaders().values(HEADER_NAME);
                            assertEquals(1, values1.size());
                            assertEquals("class", values1.get(0));
                            return new MockResponse().setBody(CONTENT);
                        case "/sample2":
                            val values2 = request.getHeaders().values(HEADER_NAME);
                            assertEquals(2, values2.size());
                            assertEquals("class", values2.get(0));
                            assertEquals("method", values2.get(1));
                            return new MockResponse().setBody(CONTENT);
                        case "/sample3":
                            val values3 = request.getHeaders().values(HEADER_NAME);
                            assertEquals(2, values3.size());
                            assertEquals("method", values3.get(0));
                            assertEquals("parameter", values3.get(1));
                            assertEquals(BODY, request.getBody().readUtf8());
                            return new MockResponse().setBody(CONTENT);
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41220);

            val client = ohLoader.getClient(InterceptorClient.class);
            assertEquals(CONTENT, client.sample1());
            assertEquals(CONTENT, client.sample2());
            assertEquals(CONTENT, client.sample3(new ParamInterceptor(), Level.BODY, BODY));

            val providerClient = ohLoader.getClient(InterceptorProviderClient.class);
            assertEquals(CONTENT, providerClient.sample1());
            assertEquals(CONTENT, providerClient.sample2());
            assertEquals(CONTENT, providerClient.sample3(new ParamInterceptor(), Level.BODY, BODY));
        }
    }

    @SneakyThrows
    @Test
    public void testError() {
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorClassLoggingClient.class));
        assertThrows(ProviderException.class, () ->
                ohLoader.getClient(ErrorClassInterceptorClient.class));

        val loggingClient = ohLoader.getClient(ErrorMethodLoggingClient.class);
        assertThrows(ProviderException.class, loggingClient::sample);
        val interceptorClient = ohLoader.getClient(ErrorMethodInterceptorClient.class);
        assertThrows(ProviderException.class, interceptorClient::sample);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor
    @ClientInterceptor(ClassInterceptor.class)
    @ClientLoggingLevel(Level.BASIC)
    public interface InterceptorClient {

        String sample1();

        @ClientInterceptor(MethodInterceptor.class)
        @ClientLoggingLevel(Level.HEADERS)
        String sample2();

        @RequestMethod(HttpMethod.POST)
        @ClientInterceptorCleanup
        @ClientInterceptor(MethodInterceptor.class)
        @ClientLoggingLevel(Level.HEADERS)
        String sample3(ParamInterceptor interceptor, Level level, @RequestBodyRaw String body);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = TestInterceptorProvider.class)
    @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
    public interface InterceptorProviderClient {

        String sample1();

        @ClientInterceptor(provider = TestInterceptorProvider.class)
        @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
        String sample2();

        @RequestMethod(HttpMethod.POST)
        @ClientInterceptorCleanup
        @ClientInterceptor(provider = TestInterceptorProvider.class)
        @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
        String sample3(ParamInterceptor interceptor, Level level, @RequestBodyRaw String body);
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientLoggingLevel(provider = ErrorClassLoggingLevelProvider.class)
    public interface ErrorClassLoggingClient {}

    @OhClient
    @Mapping("${root}:${port}")
    @ClientLoggingLevel(provider = TestLoggingLevelProvider.class)
    public interface ErrorMethodLoggingClient {

        @ClientLoggingLevel(provider = ErrorMethodLoggingLevelProvider.class)
        String sample();
    }

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = ErrorClassInterceptorProvider.class)
    public interface ErrorClassInterceptorClient {}

    @OhClient
    @Mapping("${root}:${port}")
    @ClientInterceptor(provider = TestInterceptorProvider.class)
    public interface ErrorMethodInterceptorClient {

        @ClientInterceptor(provider = ErrorMethodInterceptorProvider.class)
        String sample();
    }

    public static class TestLoggingLevelProvider implements LoggingLevelProvider {

        @Override
        public Level level(Class<?> clazz) {
            return Level.BASIC;
        }

        @Override
        public Level level(Class<?> clazz, Method method) {
            return Level.HEADERS;
        }
    }

    @EverythingIsNonNull
    public static class ClassInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "class");
            return chain.proceed(requestBuilder.build());
        }
    }

    public static class TestInterceptorProvider implements InterceptorProvider {

        @Override
        public Interceptor interceptor(Class<?> clazz) {
            return new ClassInterceptor();
        }

        @Override
        public Interceptor interceptor(Class<?> clazz, Method method) {
            return new MethodInterceptor();
        }
    }

    @EverythingIsNonNull
    public static class MethodInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "method");
            return chain.proceed(requestBuilder.build());
        }
    }

    @EverythingIsNonNull
    public static class ParamInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            val requestBuilder = chain.request().newBuilder();
            requestBuilder.addHeader(HEADER_NAME, "parameter");
            return chain.proceed(requestBuilder.build());
        }
    }

    public static class ErrorClassLoggingLevelProvider implements LoggingLevelProvider {}

    public static class ErrorMethodLoggingLevelProvider implements LoggingLevelProvider {

        @Override
        public Level level(Class<?> clazz) {
            return Level.BASIC;
        }
    }

    public static class ErrorClassInterceptorProvider implements InterceptorProvider {}

    public static class ErrorMethodInterceptorProvider implements InterceptorProvider {

        @Override
        public Interceptor interceptor(Class<?> clazz) {
            return new ClassInterceptor();
        }
    }
}
