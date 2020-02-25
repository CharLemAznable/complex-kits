package com.github.charlemaznable.core.net.ohclient;

import com.alibaba.fastjson.annotation.JSONField;
import com.github.charlemaznable.core.net.common.ContentFormat;
import com.github.charlemaznable.core.net.common.ContentFormat.FormContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.JsonContentFormatter;
import com.github.charlemaznable.core.net.common.ContentFormat.TextXmlContentFormatter;
import com.github.charlemaznable.core.net.common.FixedParameter;
import com.github.charlemaznable.core.net.common.FixedPathVar;
import com.github.charlemaznable.core.net.common.FixedValueProvider;
import com.github.charlemaznable.core.net.common.HttpMethod;
import com.github.charlemaznable.core.net.common.HttpStatus;
import com.github.charlemaznable.core.net.common.Mapping;
import com.github.charlemaznable.core.net.common.Parameter;
import com.github.charlemaznable.core.net.common.ParameterBundle;
import com.github.charlemaznable.core.net.common.RequestBodyRaw;
import com.github.charlemaznable.core.net.common.RequestMethod;
import com.github.charlemaznable.core.net.ohclient.OhFactory.OhLoader;
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

import java.lang.reflect.Method;

import static com.github.charlemaznable.core.codec.Json.unJson;
import static com.github.charlemaznable.core.codec.Xml.unXml;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ParameterTest {

    private static OhLoader ohLoader = OhFactory.ohLoader(reflectFactory());

    @SneakyThrows
    @Test
    public void testOhParameterGet() {
        try (val mockWebServer = new MockWebServer()) {
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

            val httpClient = ohLoader.getClient(GetParameterHttpClient.class);
            assertEquals("OK", httpClient.sampleDefault());
            assertEquals("OK", httpClient.sampleMapping());
            assertEquals("OK", httpClient.sampleParameters(null, "V4"));
            assertEquals("OK", httpClient.sampleBundle(new Bundle(null, null, "V4")));
            assertEquals("OK", httpClient.sampleBundle2(null));
        }
    }

    @SneakyThrows
    @Test
    public void testOhParameterPost() {
        try (val mockWebServer = new MockWebServer()) {
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

            val httpClient = ohLoader.getClient(PostParameterHttpClient.class);
            assertEquals("OK", httpClient.sampleDefault());
            assertEquals("OK", httpClient.sampleMapping());
            assertEquals("OK", httpClient.sampleParameters(null, "V4"));
            assertEquals("OK", httpClient.sampleBundle(new Bundle(null, null, "V4")));
            assertEquals("OK", httpClient.sampleBundle2(null));
            assertEquals("OK", httpClient.sampleRaw("T3=V3&T4=V4"));
            assertEquals("OK", httpClient.sampleRawError(new Object()));
        }
    }

    @FixedPathVar(name = "T0", value = "V0")
    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", valueProvider = T2Provider.class)
    @Mapping("${root}:41160")
    @OhClient
    public interface GetParameterHttpClient {

        String sampleDefault();

        @Mapping("/sampleMapping?T0={T0}")
        @FixedParameter(name = "T2", valueProvider = T2Provider.class)
        @FixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @Mapping("/sampleParameters?T0={T0}")
        @FixedParameter(name = "T2", valueProvider = T2Provider.class)
        @FixedParameter(name = "T3", value = "V3")
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@ParameterBundle Bundle bundle);

        String sampleBundle2(@ParameterBundle Bundle bundle);
    }

    @FixedParameter(name = "T1", value = "V1")
    @FixedParameter(name = "T2", valueProvider = T2Provider.class)
    @RequestMethod(HttpMethod.POST)
    @ContentFormat(FormContentFormatter.class)
    @Mapping("${root}:41161")
    @OhClient
    public interface PostParameterHttpClient {

        String sampleDefault();

        @ContentFormat(JsonContentFormatter.class)
        @FixedParameter(name = "T2", valueProvider = T2Provider.class)
        @FixedParameter(name = "T3", value = "V3")
        String sampleMapping();

        @ContentFormat(TextXmlContentFormatter.class)
        @FixedParameter(name = "T2", valueProvider = T2Provider.class)
        @FixedParameter(name = "T3", value = "V3")
        String sampleParameters(@Parameter("T3") String v3,
                                @Parameter("T4") String v4);

        String sampleBundle(@ParameterBundle Bundle bundle);

        String sampleBundle2(@ParameterBundle Bundle bundle);

        String sampleRaw(@RequestBodyRaw String raw);

        String sampleRawError(@RequestBodyRaw Object raw);
    }

    public static class T2Provider implements FixedValueProvider {

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
