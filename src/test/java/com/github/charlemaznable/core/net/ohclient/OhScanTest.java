package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.testscan.TestConfiguration;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient;
import com.github.charlemaznable.core.net.ohclient.testscan.TestHttpClient2;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class OhScanTest {

    @SneakyThrows
    @Test
    public void testOhClient() {
        val mockWebServer = new MockWebServer();
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

        val testHttpClient = SpringContext.getBean(TestHttpClient.class);
        assertEquals("Sample", testHttpClient.sample());
        assertEquals("{Sample}", testHttpClient.sampleWrapper());

        val testHttpClient2 = SpringContext.getBean(TestHttpClient2.class);
        assertNull(testHttpClient2);

        mockWebServer.shutdown();
    }
}
