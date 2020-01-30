package com.github.charlemaznable.core.net.ohclient;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.FormContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.JsonContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigContentFormat.XmlContentFormat;
import com.github.charlemaznable.core.net.ohclient.config.OhConfigRequestMethod;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedParameter;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedPathVar;
import com.github.charlemaznable.core.net.ohclient.param.OhFixedValueProvider;
import com.github.charlemaznable.core.net.ohclient.param.OhParameter;
import com.github.charlemaznable.core.net.ohclient.param.OhParameterBundle;
import com.github.charlemaznable.core.net.ohclient.param.OhRequestBodyRaw;
import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.net.ohclient.OhFactory.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OhParameterTest {

    @SneakyThrows
    @Test
    public void testOhParameterGet() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                val requestUrl = request.getRequestUrl();
                switch (requestUrl.encodedPath()) {
                    case "/sampleDefault":
                        assertNull(requestUrl.queryParameter("T0"));
                        assertEquals("V1", requestUrl.queryParameter("T1"));
                        assertEquals("V2", requestUrl.queryParameter("T2"));
                        assertNull(requestUrl.queryParameter("T3"));
                        assertNull(requestUrl.queryParameter("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleMapping":
                        assertEquals("V0", requestUrl.queryParameter("T0"));
                        assertEquals("V1", requestUrl.queryParameter("T1"));
                        assertNull(requestUrl.queryParameter("T2"));
                        assertEquals("V3", requestUrl.queryParameter("T3"));
                        assertNull(requestUrl.queryParameter("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleParameters":
                        assertEquals("V0", requestUrl.queryParameter("T0"));
                        assertEquals("V1", requestUrl.queryParameter("T1"));
                        assertNull(requestUrl.queryParameter("T2"));
                        assertNull(requestUrl.queryParameter("T3"));
                        assertEquals("V4", requestUrl.queryParameter("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleBundle":
                        assertEquals("V1", requestUrl.queryParameter("T1"));
                        assertNull(requestUrl.queryParameter("T2"));
                        assertNull(requestUrl.queryParameter("T3"));
                        assertEquals("V4", requestUrl.queryParameter("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleBundle2":
                        assertEquals("V1", requestUrl.queryParameter("T1"));
                        assertEquals("V2", requestUrl.queryParameter("T2"));
                        assertNull(requestUrl.queryParameter("T3"));
                        assertNull(requestUrl.queryParameter("T4"));
                        return new MockResponse().setBody("OK");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41160);

        val httpClient = getClient(GetParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleParameters(null, "V4"));
        assertEquals("OK", httpClient.sampleBundle(new Bundle(null, null, "V4")));
        assertEquals("OK", httpClient.sampleBundle2(null));

        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void testOhParameterPost() {
        val mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) {
                val body = request.getBody().readUtf8();
                switch (request.getPath()) {
                    case "/sampleDefault":
                        val defaultMap = Splitter.on("&")
                                .withKeyValueSeparator("=").split(body);
                        assertEquals("V1", defaultMap.get("T1"));
                        assertEquals("V2", defaultMap.get("T2"));
                        assertNull(defaultMap.get("T3"));
                        assertNull(defaultMap.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleMapping":
                        val mappingMap = unJson(body);
                        assertEquals("V1", mappingMap.get("T1"));
                        assertNull(mappingMap.get("T2"));
                        assertEquals("V3", mappingMap.get("T3"));
                        assertNull(mappingMap.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleParameters":
                        val paramMap = unXml(body);
                        assertEquals("V1", paramMap.get("T1"));
                        assertNull(paramMap.get("T2"));
                        assertNull(paramMap.get("T3"));
                        assertEquals("V4", paramMap.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleBundle":
                        val bundleMap = Splitter.on("&")
                                .withKeyValueSeparator("=").split(body);
                        assertEquals("V1", bundleMap.get("T1"));
                        assertNull(bundleMap.get("T2"));
                        assertNull(bundleMap.get("T3"));
                        assertEquals("V4", bundleMap.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleBundle2":
                        val bundleMap2 = Splitter.on("&")
                                .withKeyValueSeparator("=").split(body);
                        assertEquals("V1", bundleMap2.get("T1"));
                        assertEquals("V2", bundleMap2.get("T2"));
                        assertNull(bundleMap2.get("T3"));
                        assertNull(bundleMap2.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleRaw":
                        val rawMap = Splitter.on("&")
                                .withKeyValueSeparator("=").split(body);
                        assertNull(rawMap.get("T1"));
                        assertNull(rawMap.get("T2"));
                        assertEquals("V3", rawMap.get("T3"));
                        assertEquals("V4", rawMap.get("T4"));
                        return new MockResponse().setBody("OK");
                    case "/sampleRawError":
                        val rawErrorMap = Splitter.on("&")
                                .withKeyValueSeparator("=").split(body);
                        assertEquals("V1", rawErrorMap.get("T1"));
                        assertEquals("V2", rawErrorMap.get("T2"));
                        assertNull(rawErrorMap.get("T3"));
                        assertNull(rawErrorMap.get("T4"));
                        return new MockResponse().setBody("OK");
                }
                return new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setBody(HttpStatus.NOT_FOUND.getReasonPhrase());
            }
        });
        mockWebServer.start(41161);

        val httpClient = getClient(PostParameterHttpClient.class);
        assertEquals("OK", httpClient.sampleDefault());
        assertEquals("OK", httpClient.sampleMapping());
        assertEquals("OK", httpClient.sampleParameters(null, "V4"));
        assertEquals("OK", httpClient.sampleBundle(new Bundle(null, null, "V4")));
        assertEquals("OK", httpClient.sampleBundle2(null));
        assertEquals("OK", httpClient.sampleRaw("T3=V3&T4=V4"));
        assertEquals("OK", httpClient.sampleRawError(new Object()));

        mockWebServer.shutdown();
    }

    @OhFixedPathVar(name = "T0", value = "V0")
    @OhFixedParameter(name = "T1", value = "V1")
    @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
    @OhClient("${root}:41160")
    public interface GetParameterHttpClient {

        String sampleDefault();

        @OhMapping("/sampleMapping?T0={T0}")
        @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
        @OhFixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @OhMapping("/sampleParameters?T0={T0}")
        @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
        @OhFixedParameter(name = "T3", value = "V3")
        String sampleParameters(@OhParameter("T3") String v3,
                                @OhParameter("T4") String v4);

        String sampleBundle(@OhParameterBundle Bundle bundle);

        String sampleBundle2(@OhParameterBundle Bundle bundle);
    }

    @OhFixedParameter(name = "T1", value = "V1")
    @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
    @OhConfigRequestMethod(RequestMethod.POST)
    @OhConfigContentFormat(FormContentFormat.class)
    @OhClient("${root}:41161")
    public interface PostParameterHttpClient {

        String sampleDefault();

        @OhConfigContentFormat(JsonContentFormat.class)
        @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
        @OhFixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @OhConfigContentFormat(XmlContentFormat.class)
        @OhFixedParameter(name = "T2", valueProvider = T2Provider.class)
        @OhFixedParameter(name = "T3", value = "V3")
        String sampleParameters(@OhParameter("T3") String v3,
                                @OhParameter("T4") String v4);

        String sampleBundle(@OhParameterBundle Bundle bundle);

        String sampleBundle2(@OhParameterBundle Bundle bundle);

        String sampleRaw(@OhRequestBodyRaw String raw);

        String sampleRawError(@OhRequestBodyRaw Object raw);
    }

    public static class T2Provider implements OhFixedValueProvider {

        @Override
        public String value(Class<?> clazz, String name) {
            return "V2";
        }

        @Override
        public String value(Class<?> clazz, Method method, String name) {
            return null;
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Bundle {

        @JSONField(name = "T2")
        private String t2;
        @JSONField(name = "T3")
        private String t3;
        @JSONField(name = "T4")
        private String t4;
    }
}
