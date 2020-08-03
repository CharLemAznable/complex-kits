package com.github.charlemaznable.core.net.ohclient.spring;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.testclient.TestComponentSpring;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OhSpringErrorConfiguration.class)
public class OhSpringErrorTest {

    @Autowired
    private TestComponentSpring testComponent;

    @SneakyThrows
    @Test
    public void testOhClientError() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("SampleError");
                        case "/sampleError":
                            return new MockResponse().setBody("Sample");
                        default:
                            return new MockResponse()
                                    .setResponseCode(HttpStatus.NOT_FOUND.value())
                                    .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                    }
                }
            });
            mockWebServer.start(41102);

            val testHttpClient = testComponent.getTestHttpClient();
            assertThrows(NullPointerException.class, testHttpClient::sample);
            assertThrows(NullPointerException.class, testHttpClient::sampleWrapper);
            assertEquals("Sample", testHttpClient.sampleWrap());

            val testHttpClientIsolated = SpringContext.getBean(TestHttpClientIsolated.class);
            assertEquals("SampleError", testHttpClientIsolated.sample());
            assertEquals("[SampleError]", testHttpClientIsolated.sampleWrapper());

            val testHttpClientConcrete = SpringContext.getBean(TestHttpClientConcrete.class);
            assertNull(testHttpClientConcrete);

            val testHttpClientNone = SpringContext.getBean(TestHttpClientNone.class);
            assertNull(testHttpClientNone);
        }
    }
}
