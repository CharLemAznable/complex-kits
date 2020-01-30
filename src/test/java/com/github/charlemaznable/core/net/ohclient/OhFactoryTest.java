package com.github.charlemaznable.core.net.ohclient;

import com.github.charlemaznable.core.net.ohclient.config.OhConfigAcceptCharset;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.FormContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.JsonContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.XmlContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigRequestMethod;
import com.github.charlemaznable.core.net.ohclient.exception.OhException;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.joor.ReflectException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.ACCEPT_CHARSET;
import static com.github.charlemaznable.core.net.ohclient.internal.OhConstant.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

public class OhFactoryTest {

    @Test
    public void testOhFactory() {
        assertThrows(ReflectException.class, () -> onClass(OhFactory.class).create().get());

        assertThrows(OhException.class, () -> getClient(TestNotInterface.class));
    }

    @SneakyThrows
    @Test
    public void testAcceptCharset() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sample":
                        val acceptCharset = request.getHeader(ACCEPT_CHARSET);
                        assertEquals(ISO_8859_1.name(), acceptCharset);
                        return new MockResponse().setBody(acceptCharset);
                    case "/sample2":
                        val acceptCharset2 = request.getHeader(ACCEPT_CHARSET);
                        assertEquals(UTF_8.name(), acceptCharset2);
                        return new MockResponse().setBody(acceptCharset2);
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41130);

        val httpClient = getClient(AcceptCharsetHttpClient.class);
        assertEquals(ISO_8859_1.name(), httpClient.sample());
        assertEquals(UTF_8.name(), httpClient.sample2());

        assertEquals("OhClient@" + httpClient.hashCode(), httpClient.toString());
        assertEquals(httpClient, getClient(AcceptCharsetHttpClient.class));
        assertEquals(httpClient.hashCode(), getClient(AcceptCharsetHttpClient.class).hashCode());

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testContentFormat() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sample":
                        val contentType = request.getHeader(CONTENT_TYPE);
                        assertTrue(contentType.startsWith(APPLICATION_FORM_URLENCODED_VALUE));
                        val bodyString = request.getBody().readUtf8();
                        return new MockResponse().setBody(bodyString);
                    case "/sample2":
                        val contentType2 = request.getHeader(CONTENT_TYPE);
                        assertTrue(contentType2.startsWith(APPLICATION_JSON_VALUE));
                        val bodyString2 = request.getBody().readUtf8();
                        return new MockResponse().setBody(bodyString2);
                    case "/sample3":
                        val contentType3 = request.getHeader(CONTENT_TYPE);
                        assertTrue(contentType3.startsWith(APPLICATION_XML_VALUE));
                        val bodyString3 = request.getBody().readUtf8();
                        return new MockResponse().setBody(bodyString3);
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41131);

        val httpClient = getClient(ContentFormatHttpClient.class);
        assertEquals("", httpClient.sample());
        assertEquals("{}", httpClient.sample2());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xml/>", httpClient.sample3());

        assertEquals("OhClient@" + httpClient.hashCode(), httpClient.toString());
        assertEquals(httpClient, getClient(ContentFormatHttpClient.class));
        assertEquals(httpClient.hashCode(), getClient(ContentFormatHttpClient.class).hashCode());

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testRequestMethod() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sample":
                        val method = request.getMethod();
                        assertEquals("POST", method);
                        return new MockResponse().setBody(method);
                    case "/sample2":
                        val method2 = request.getMethod();
                        assertEquals("GET", method2);
                        return new MockResponse().setBody(method2);
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41132);

        val httpClient = getClient(RequestMethodHttpClient.class);
        assertEquals("POST", httpClient.sample());
        assertEquals("GET", httpClient.sample2());

        assertEquals("OhClient@" + httpClient.hashCode(), httpClient.toString());
        assertEquals(httpClient, getClient(RequestMethodHttpClient.class));
        assertEquals(httpClient.hashCode(), getClient(RequestMethodHttpClient.class).hashCode());

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testExtendInterface() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                switch (request.getPath()) {
                    case "/sample":
                        return new MockResponse().setBody("OK");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41133);

        val baseHttpClient = getClient(BaseHttpClient.class);
        assertNotNull(baseHttpClient);

        assertThrows(OhException.class, () -> getClient(SubHttpClient.class));

        mockWebServer.shutdown();
    }

    @OhConfigAcceptCharset("ISO-8859-1")
    @OhMapping("${root}:41130")
    @OhClient
    public interface AcceptCharsetHttpClient {

        String sample();

        @OhConfigAcceptCharset("UTF-8")
        String sample2();
    }

    @OhConfigRequestMethod(RequestMethod.POST)
    @OhConfigContentFormat(FormContentFormat.class)
    @OhMapping("${root}:41131")
    @OhClient
    public interface ContentFormatHttpClient {

        String sample();

        @OhConfigContentFormat(JsonContentFormat.class)
        String sample2();

        @OhConfigContentFormat(XmlContentFormat.class)
        String sample3();
    }

    @OhConfigRequestMethod(RequestMethod.POST)
    @OhMapping("${root}:41132")
    @OhClient
    public interface RequestMethodHttpClient {

        String sample();

        @OhConfigRequestMethod(RequestMethod.GET)
        String sample2();
    }

    @OhMapping("${root}:41133")
    @OhClient
    public interface BaseHttpClient {}

    public interface SubHttpClient extends BaseHttpClient {}

    public static class TestNotInterface {}
}
