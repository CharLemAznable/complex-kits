package com.github.charlemaznable.core.net.ohclient.spring;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.testclient.TestComponent;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientConcrete;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientIsolated;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClientNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OhSpringConfiguration.class)
public class OhSpringTest {

    private static final String SAMPLE = "Sample";
    @Autowired
    private TestComponent testComponent;

    @SneakyThrows
    @Test
    public void testOhClient() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody(SAMPLE);
                        case "/SpringSpring-SpringSpring-GuiceGuice":
                            return new MockResponse().setBody("Done");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val testHttpClient = testComponent.getTestHttpClient();
            assertEquals(SAMPLE, testHttpClient.sample());
            assertEquals("{Sample}", testHttpClient.sampleWrapper());
            assertEquals(SAMPLE, testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());
            assertEquals(SAMPLE, testHttpClient.sample());
            assertEquals("{Sample}", testHttpClient.sampleWrapper());
            assertEquals(SAMPLE, testHttpClient.sampleWrap());
            assertEquals("Done", testHttpClient.sampleByContext());

            val testHttpClientIsolated = SpringContext.getBean(TestHttpClientIsolated.class);
            assertEquals(SAMPLE, testHttpClientIsolated.sample());
            assertEquals("[Sample]", testHttpClientIsolated.sampleWrapper());

            val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
            assertNull(testHttpClientConcrete);

            val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
            assertNull(testHttpClientNone);
        }
    }
}
