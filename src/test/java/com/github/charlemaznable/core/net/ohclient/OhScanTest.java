package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.testscan.TestComponent;
import com.github.charlemaznable.core.net.ohclient.testscan.TestConfiguration;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient2;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient3;
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
@ContextConfiguration(classes = TestConfiguration.class)
public class OhScanTest {

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
                            return new MockResponse().setBody("Sample");
                    }
                    return new MockResponse()
                            .setResponseCode(HttpStatus.NOT_FOUND.value())
                            .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
                }
            });
            mockWebServer.start(41102);

            val testHttpClient = testComponent.testHttpClient;
            assertEquals("Sample", testHttpClient.sample());
            assertEquals("{Sample}", testHttpClient.sampleWrapper());

            val testHttpClient2 = SpringContext.getBean(TestHttpClient2.class);
            assertEquals("Sample", testHttpClient2.sample());
            assertEquals("[Sample]", testHttpClient2.sampleWrapper());

            val testHttpClient3 = SpringContext.getBean(TestHttpClient3.class);
            assertNull(testHttpClient3);
        }
    }
}
