package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.miner.MinerInjector;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.testscan.TestComponent;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient2;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient3;
import com.github.charlemaznable.core.net.ohclient.testscan.TestSampleUrlProvider;
import com.google.inject.Guice;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhGuiceTest {

    @SneakyThrows
    @Test
    public void testOhClient() {
        val minerInjector = new MinerInjector();
        val minerModule = minerInjector.minerModule(TestSampleUrlProvider.class);
        val ohInjector = new OhInjector(minerModule);
        var injector = ohInjector.injectOhClient(
                TestHttpClient.class, TestHttpClient2.class);

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("Guice");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponent.class);
            val testHttpClient = testComponent.testHttpClient;
            assertEquals("Guice", testHttpClient.sample());
            assertEquals("{Guice}", testHttpClient.sampleWrapper());
            assertEquals("Guice", testHttpClient.sampleWrap());

            var testHttpClient2 = injector.getInstance(TestHttpClient2.class);
            assertEquals("Guice", testHttpClient2.sample());
            assertEquals("[Guice]", testHttpClient2.sampleWrapper());

            testHttpClient2 = ohInjector.getClient(TestHttpClient2.class);
            assertEquals("Guice", testHttpClient2.sample());
            assertEquals("[Guice]", testHttpClient2.sampleWrapper());

            assertThrows(OhException.class, () ->
                    ohInjector.getClient(TestHttpClient3.class));
        }
    }

    @SneakyThrows
    @Test
    public void testOhClientError() {
        val ohInjector = new OhInjector();
        var injector = Guice.createInjector(ohInjector.ohModule(
                TestHttpClient.class, TestHttpClient2.class));

        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sampleError":
                            return new MockResponse().setBody("GuiceError");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testComponent = injector.getInstance(TestComponent.class);
            val testHttpClient = testComponent.testHttpClient;
            assertEquals("GuiceError", testHttpClient.sampleWrap());
        }
    }
}
