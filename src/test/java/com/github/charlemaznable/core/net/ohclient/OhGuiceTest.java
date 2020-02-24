package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.miner.MinerInjector;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.testscan.TestComponent;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient2;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient3;
import com.github.charlemaznable.core.net.ohclient.testscan.TestSampleUrlProvider;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OhGuiceTest {

    @SneakyThrows
    @Test
    public void testOhClient() {
        val minerInjector = new MinerInjector(Guice.createInjector());
        val minerModule = minerInjector.createModule(TestSampleUrlProvider.class);
        val ohInjector = new OhInjector(minerModule);
        var injector = ohInjector.createInjector(
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
        val ohInjector1 = new OhInjector(newArrayList());
        var injector1 = Guice.createInjector(ohInjector1.createModule(
                TestHttpClient.class, TestHttpClient2.class));

        val ohInjector2 = new OhInjector(Guice.createInjector());
        var injector2 = Guice.createInjector(ohInjector2.createModule(
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

            val testComponent1 = injector1.getInstance(TestComponent.class);
            val testHttpClient1 = testComponent1.testHttpClient;
            assertThrows(ConfigurationException.class, testHttpClient1::sample);

            val testComponent2 = injector2.getInstance(TestComponent.class);
            val testHttpClient2 = testComponent2.testHttpClient;
            assertEquals("GuiceError", testHttpClient2.sampleWrap());
        }
    }
}
