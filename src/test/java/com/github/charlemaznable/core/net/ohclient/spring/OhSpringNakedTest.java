package com.github.charlemaznable.core.net.ohclient.spring;

import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.ohclient.OhException;
import com.github.charlemaznable.core.net.ohclient.testclient.TestComponentSpring;
import com.github.charlemaznable.core.net.ohclient.testclient.TestHttpClient;
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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OhSpringNakedConfiguration.class)
public class OhSpringNakedTest {

    @Autowired
    private TestComponentSpring testComponent;

    @SneakyThrows
    @Test
    public void testOhClientNaked() {
        try (val mockWebServer = new MockWebServer()) {
            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    switch (request.getPath()) {
                        case "/sample":
                            return new MockResponse().setBody("SampleError");
                        case "/sampleError":
                            return new MockResponse().setBody("SampleNoError");
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
            assertEquals("SampleNoError", testHttpClient.sampleWrap());

            val testHttpClientIsolated = getClient(TestHttpClientIsolated.class);
            assertEquals("SampleError", testHttpClientIsolated.sample());
            assertEquals("[SampleError]", testHttpClientIsolated.sampleWrapper());

            assertThrows(OhException.class,
                    () -> getClient(TestHttpClientConcrete.class));

            assertThrows(OhException.class,
                    () -> getClient(TestHttpClientNone.class));

            ApplicationContext applicationContext = onClass(SpringContext.class)
                    .field("applicationContext").get();
            assertThrows(NoSuchBeanDefinitionException.class, () ->
                    applicationContext.getBean(TestHttpClient.class));
            assertNull(SpringContext.getBeanOrCreate(TestHttpClient.class));
        }
    }
}
