package com.github.charlemaznable.core.net.ohclient.guice;

import com.github.charlemaznable.core.miner.MinerInjector;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.OhInjector;
import com.github.charlemaznable.core.net.ohclient.testclient.TestComponent;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClient;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientIsolated;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientNone;
import com.github.charlemaznable.core.net.ohclient.testclient.TestSampleUrlProvider;
import com.google.inject.Guice;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhGuiceTest {

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @SneakyThrows
    @Test
    public void testOhClient() {
        val minerInjector = new MinerInjector();
        val minerModule = minerInjector.createModule(TestSampleUrlProvider.class);
        val ohInjector = new OhInjector(minerModule);
        var injector = ohInjector.createInjector(
                TestHttpClient.class, TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class);

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("Guice");
                        case "/GuiceGuice-SpringSpring-GuiceGuice":
                            return new MockResponse().setBody("Done");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponent.class);
            val testHttpClient = testComponent.getTestHttpClient();
            assertEquals("Guice", testHttpClient.sample());
            assertEquals("{Guice}", testHttpClient.sampleWrapper());
            assertEquals("Guice", testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());
            assertEquals("Guice", testHttpClient.sample());
            assertEquals("{Guice}", testHttpClient.sampleWrapper());
            assertEquals("Guice", testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals("Guice", testHttpClient2.sample());
            assertEquals("[Guice]", testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertNull(injector.getInstance(TestHttpClientNone.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientError() {
        val ohInjector = new OhInjector(emptyList());
        var injector = Guice.createInjector(ohInjector.createModule(
                TestHttpClient.class, TestHttpClientIsolated.class,
                TestHttpClientConcrete.class, TestHttpClientNone.class));

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("GuiceError");
                        case "/sampleError":
                            return new MockResponse().setBody("Guice");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponent.class);
            val testHttpClient = testComponent.getTestHttpClient();
            assertThrows(NullPointerException.class, testHttpClient::sample);
            assertThrows(NullPointerException.class, testHttpClient::sampleWrapper);
            assertEquals("Guice", testHttpClient.sampleWrap());

            val testHttpClient2 = injector.getInstance(TestHttpClientIsolated.class);
            assertEquals("GuiceError", testHttpClient2.sample());
            assertEquals("[GuiceError]", testHttpClient2.sampleWrapper());

            assertNull(injector.getInstance(TestHttpClientConcrete.class));

            assertNull(injector.getInstance(TestHttpClientNone.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientNaked() {
        val ohInjector = new OhInjector();

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("GuiceError");
                        case "/sampleError":
                            return new MockResponse().setBody("GuiceNoError");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testHttpClient = ohInjector.getClient(TestHttpClient.class);
            assertThrows(NullPointerException.class, testHttpClient::sample);
            assertThrows(NullPointerException.class, testHttpClient::sampleWrapper);
            assertEquals("GuiceNoError", testHttpClient.sampleWrap());

            val testHttpClient2 = ohInjector.getClient(TestHttpClientIsolated.class);
            assertEquals("GuiceError", testHttpClient2.sample());
            assertEquals("[GuiceError]", testHttpClient2.sampleWrapper());

            assertThrows(OhException.class,
                    () -> ohInjector.getClient(TestHttpClientConcrete.class));

            assertThrows(OhException.class,
                    () -> ohInjector.getClient(TestHttpClientNone.class));
        }
    }
}
